package edu.ncsu.csc216.pack_scheduler.manager;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.circa.mrv.grs_manager.catalog.NioxCatalog;
import com.circa.mrv.grs_manager.directory.CustomerDirectory;
import com.circa.mrv.grs_manager.directory.Product;
import com.circa.mrv.grs_manager.manager.GRSManager;
import com.circa.mrv.grs_manager.niox.Mino;
import com.circa.mrv.grs_manager.directory.VendorDirectory;
import com.circa.mrv.grs_manager.user.Employee;
import com.circa.mrv.grs_manager.user.User;
import com.circa.mrv.grs_manager.user.schedule.CustomerSchedule;
import com.circa.mrv.grs_manager.user.schedule.VendorSchedule;

/**
 * the registration manager test class provides a number of tests of the functionallity of the registration manager class.
 * @author Ben
 */
public class RegistrationManagerTest {
	
	private GRSManager manager;
	private NioxCatalog catalog;
	private VendorDirectory directory;
	private CustomerDirectory fDirectory;
	private String validCourseRecordsFile = "test-files/course_records.txt";
	private String validStudentRecordsFile = "test-files/student_records.txt";
	/**
	 * Sets up the CourseManager and clears the data.
	 * @throws Exception if error
	 */
	@Before
	public void setUp() throws Exception {
		manager = GRSManager.getInstance();
		manager.clearData();
	}

	/**
	 * tests that registrationManager is constructed properly
	 */
	@Test
	public void testGetInstance(){
		catalog = manager.getNioxCatalog();
		directory = manager.getVendorDirectory();
		
		assertEquals(0, catalog.getNioxCatalog().length);
		assertEquals(0, directory.getStudentDirectory().length);
		assertTrue(manager.login("registrar", "Regi5tr@r"));	
	}
	
	/**
	 * Tests that a valid course catalog is constructed and can be obtain by the get course catalog method. Verifies correct catalog by length.
	 */
	@Test
	public void testGetCourseCatalog() {
		catalog = manager.getNioxCatalog();
		catalog.loadProductsFromFile(validCourseRecordsFile);
		//catalog = manager.getCourseCatalog();
		assertEquals(8, catalog.getNioxCatalog().length);
	}

	/**
	 * 	 Tests that a valid student directory is constructed and can be obtain by the get student directory method. Verifies correct directory by length. 
	 */
	@Test
	public void testGetStudentDirectory() {
		directory = manager.getVendorDirectory();
		directory.loadStudentsFromFile(validStudentRecordsFile);
		directory = manager.getVendorDirectory();
		assertEquals(10, directory.getStudentDirectory().length);
	}

	/**
	 * Tests that a student in the directory can log in, that one not in the directory can not log in, and that the registrar can log in. 
	 */
	@Test
	public void testLogin() {
		directory = manager.getVendorDirectory();
		directory.loadStudentsFromFile(validStudentRecordsFile);
		directory = manager.getVendorDirectory();
		directory.addStudent("Ben", "Iop", "biop", "biop@mail.com", "123", "123", 15);
		manager.logout();
		assertTrue(manager.login("registrar", "Regi5tr@r"));
		assertFalse(manager.login("biop", "123"));
		manager.logout();
		assertFalse(manager.login("registrar", "pass"));	

		manager.logout();
		assertTrue(manager.login("biop", "123"));
		manager.logout();
		try{
		assertFalse(manager.login("johnDoe", "123"));
		fail();
		} catch (IllegalArgumentException e){
		assertEquals(null, manager.getCurrentUser());
		}
	}

	/**
	 * tests that when the current user logs out that the current user parameter is set to null.
	 */
	@Test
	public void testLogout() {
		manager.logout();
		assertEquals(null, manager.getCurrentUser());
	}

	/**
	 * Tests that the correct current user is obtained.
	 */
	@Test
	public void testGetCurrentUser() {
		directory = manager.getVendorDirectory();
		directory.addStudent("Ben", "Iop", "biop", "biop@mail.com", "123", "123", 15);
		manager.logout();
		manager.login("registrar", "Regi5tr@r");	
		assertEquals("registrar", manager.getCurrentUser().getId());
		manager.logout();
		manager.login("biop", "123");
		assertEquals("biop", manager.getCurrentUser().getId());
		manager.logout();
		assertEquals(null, manager.getCurrentUser());
	}

	   /**
	 * Tests RegistrationManager.enrollStudentInCourse()
	 */
	@Test
	public void testEnrollStudentInCourse() {
	    directory = manager.getVendorDirectory();
	    directory.loadStudentsFromFile("test-files/student_records.txt");
	    
	    catalog = manager.getNioxCatalog();
	    catalog.loadProductsFromFile("test-files/course_records.txt");
	    
	    manager.logout(); //In case not handled elsewhere
	    
	    //test if not logged in
	    try {
	        manager.enrollStudentInCourse(catalog.getProductFromCatalog("CSC216", "001"));
	        fail("RegistrationManager.enrollStudentInCourse() - If the current user is null, an IllegalArgumentException should be thrown, but was not.");
	    } catch (IllegalArgumentException e) {
	        assertNull("RegistrationManager.enrollStudentInCourse() - currentUser is null, so cannot enroll in course.", manager.getCurrentUser());
	    }
	    
	    //test if registrar is logged in
	    manager.login("registrar", "Regi5tr@r");
	    try {
	        manager.enrollStudentInCourse(catalog.getProductFromCatalog("CSC216", "001"));
	        fail("RegistrationManager.enrollStudentInCourse() - If the current user is registrar, an IllegalArgumentException should be thrown, but was not.");
	    } catch (IllegalArgumentException e) {
	        assertEquals("RegistrationManager.enrollStudentInCourse() - currentUser is registrar, so cannot enroll in course.", "registrar", manager.getCurrentUser().getId());
	    }
	    manager.logout();
	    
	    manager.login("efrost", "pw");
	    assertFalse("Action: enroll\nUser: efrost\nCourse: CSC216-001\nResults: False - Student max credits are 3 and course has 4.", manager.enrollStudentInCourse(catalog.getProductFromCatalog("CSC216", "001")));
	    assertTrue("Action: enroll\nUser: efrost\nCourse: CSC226-001\nResults: True", manager.enrollStudentInCourse(catalog.getProductFromCatalog("CSC226", "001")));
	    assertFalse("Action: enroll\nUser: efrost\nCourse: CSC226-001, CSC230-001\nResults: False - cannot exceed max student credits.", manager.enrollStudentInCourse(catalog.getProductFromCatalog("CSC230", "001")));
	    
	    //Check Student Schedule
	    Product efrost = directory.getEmployeeById("efrost");
	    VendorSchedule scheduleFrost = efrost.getSchedule();
	    assertEquals(3, scheduleFrost.getScheduleCredits());
	    String[][] scheduleFrostArray = scheduleFrost.getScheduledCourses();
	    assertEquals(1, scheduleFrostArray.length);
	    assertEquals("CSC226", scheduleFrostArray[0][0]);
	    assertEquals("001", scheduleFrostArray[0][1]);
	    assertEquals("Discrete Mathematics for Computer Scientists", scheduleFrostArray[0][2]);
	    assertEquals("MWF 9:35AM-10:25AM", scheduleFrostArray[0][3]);
	    assertEquals("9", scheduleFrostArray[0][4]);
	            
	    manager.logout();
	    
	    manager.login("ahicks", "pw");
	    assertTrue("Action: enroll\nUser: ahicks\nCourse: CSC216-001\nResults: True", manager.enrollStudentInCourse(catalog.getProductFromCatalog("CSC216", "001")));
	    assertTrue("Action: enroll\nUser: ahicks\nCourse: CSC216-001, CSC226-001\nResults: True", manager.enrollStudentInCourse(catalog.getProductFromCatalog("CSC226", "001")));
	    assertFalse("Action: enroll\nUser: ahicks\nCourse: CSC216-001, CSC226-001, CSC226-001\nResults: False - duplicate", manager.enrollStudentInCourse(catalog.getProductFromCatalog("CSC226", "001")));
	    assertFalse("Action: enroll\nUser: ahicks\nCourse: CSC216-001, CSC226-001, CSC116-001\nResults: False - time conflict", manager.enrollStudentInCourse(catalog.getProductFromCatalog("CSC116", "001")));
	    assertTrue("Action: enroll\nUser: ahicks\nCourse: CSC216-001, CSC226-001, CSC116-003\nResults: True", manager.enrollStudentInCourse(catalog.getProductFromCatalog("CSC116", "003")));
	    assertFalse("Action: enroll\nUser: ahicks\nCourse: CSC216-001, CSC226-001, CSC116-003, CSC116-002\nResults: False - already in section of 116", manager.enrollStudentInCourse(catalog.getProductFromCatalog("CSC216", "601")));
	    assertFalse("Action: enroll\nUser: ahicks\nCourse: CSC216-001, CSC226-001, CSC116-003, CSC230-001\nResults: False - exceeded max credits", manager.enrollStudentInCourse(catalog.getProductFromCatalog("CSC230", "001")));
	    
	    //Check Student Schedule
	    Product ahicks = directory.getEmployeeById("ahicks");
	    VendorSchedule scheduleHicks = ahicks.getSchedule();
	    assertEquals(10, scheduleHicks.getScheduleCredits());
	    String[][] scheduleHicksArray = scheduleHicks.getScheduledCourses();
	    assertEquals(3, scheduleHicksArray.length);
	    assertEquals("CSC216", scheduleHicksArray[0][0]);
	    assertEquals("001", scheduleHicksArray[0][1]);
	    assertEquals("Programming Concepts - Java", scheduleHicksArray[0][2]);
	    assertEquals("TH 1:30PM-2:45PM", scheduleHicksArray[0][3]);
	    assertEquals("9", scheduleHicksArray[0][4]);
	    assertEquals("CSC226", scheduleHicksArray[1][0]);
	    assertEquals("001", scheduleHicksArray[1][1]);
	    assertEquals("Discrete Mathematics for Computer Scientists", scheduleHicksArray[1][2]);
	    assertEquals("MWF 9:35AM-10:25AM", scheduleHicksArray[1][3]);
	    assertEquals("8", scheduleHicksArray[1][4]);
	    assertEquals("CSC116", scheduleHicksArray[2][0]);
	    assertEquals("003", scheduleHicksArray[2][1]);
	    assertEquals("Intro to Programming - Java", scheduleHicksArray[2][2]);
	    assertEquals("TH 11:20AM-1:10PM", scheduleHicksArray[2][3]);
	    assertEquals("9", scheduleHicksArray[2][4]);
	    
	    manager.logout();
	}

	/**
	 * Tests RegistrationManager.dropStudentFromCourse()
	 */
	@Test
	public void testDropStudentFromCourse() {
	    directory = manager.getVendorDirectory();
	    directory.loadStudentsFromFile("test-files/student_records.txt");
	    
	    catalog = manager.getNioxCatalog();
	    catalog.loadProductsFromFile("test-files/course_records.txt");
	    
	    manager.logout(); //In case not handled elsewhere
	    
	    //test if not logged in
	    try {
	        manager.dropStudentFromCourse(catalog.getProductFromCatalog("CSC216", "001"));
	        fail("RegistrationManager.dropStudentFromCourse() - If the current user is null, an IllegalArgumentException should be thrown, but was not.");
	    } catch (IllegalArgumentException e) {
	        assertNull("RegistrationManager.dropStudentFromCourse() - currentUser is null, so cannot enroll in course.", manager.getCurrentUser());
	    }
	    
	    //test if registrar is logged in
	    manager.login("registrar", "Regi5tr@r");
	    try {
	        manager.dropStudentFromCourse(catalog.getProductFromCatalog("CSC216", "001"));
	        fail("RegistrationManager.dropStudentFromCourse() - If the current user is registrar, an IllegalArgumentException should be thrown, but was not.");
	    } catch (IllegalArgumentException e) {
	        assertEquals("RegistrationManager.dropStudentFromCourse() - currentUser is registrar, so cannot enroll in course.", "registrar", manager.getCurrentUser().getId());
	    }
	    manager.logout();
	    
	    manager.login("efrost", "pw");
	    assertFalse("Action: enroll\nUser: efrost\nCourse: CSC216-001\nResults: False - Student max credits are 3 and course has 4.", manager.enrollStudentInCourse(catalog.getProductFromCatalog("CSC216", "001")));
	    assertTrue("Action: enroll\nUser: efrost\nCourse: CSC226-001\nResults: True", manager.enrollStudentInCourse(catalog.getProductFromCatalog("CSC226", "001")));
	    assertFalse("Action: enroll\nUser: efrost\nCourse: CSC226-001, CSC230-001\nResults: False - cannot exceed max student credits.", manager.enrollStudentInCourse(catalog.getProductFromCatalog("CSC230", "001")));
	    
	    assertFalse("Action: drop\nUser: efrost\nCourse: CSC216-001\nResults: False - student not enrolled in the course", manager.dropStudentFromCourse(catalog.getProductFromCatalog("CSC216", "001")));
	    assertTrue("Action: drop\nUser: efrost\nCourse: CSC226-001\nResults: True", manager.dropStudentFromCourse(catalog.getProductFromCatalog("CSC226", "001")));
	    
	    //Check Student Schedule
	    Product efrost = directory.getEmployeeById("efrost");
	    VendorSchedule scheduleFrost = efrost.getSchedule();
	    assertEquals(0, scheduleFrost.getScheduleCredits());
	    String[][] scheduleFrostArray = scheduleFrost.getScheduledCourses();
	    assertEquals(0, scheduleFrostArray.length);
	    
	    manager.logout();
	    
	    manager.login("ahicks", "pw");
	    assertTrue("Action: enroll\nUser: ahicks\nCourse: CSC216-001\nResults: True", manager.enrollStudentInCourse(catalog.getProductFromCatalog("CSC216", "001")));
	    assertTrue("Action: enroll\nUser: ahicks\nCourse: CSC216-001, CSC226-001\nResults: True", manager.enrollStudentInCourse(catalog.getProductFromCatalog("CSC226", "001")));
	    assertFalse("Action: enroll\nUser: ahicks\nCourse: CSC216-001, CSC226-001, CSC226-001\nResults: False - duplicate", manager.enrollStudentInCourse(catalog.getProductFromCatalog("CSC226", "001")));
	    assertFalse("Action: enroll\nUser: ahicks\nCourse: CSC216-001, CSC226-001, CSC116-001\nResults: False - time conflict", manager.enrollStudentInCourse(catalog.getProductFromCatalog("CSC116", "001")));
	    assertTrue("Action: enroll\nUser: ahicks\nCourse: CSC216-001, CSC226-001, CSC116-003\nResults: True", manager.enrollStudentInCourse(catalog.getProductFromCatalog("CSC116", "003")));
	    assertFalse("Action: enroll\nUser: ahicks\nCourse: CSC216-001, CSC226-001, CSC116-003, CSC116-002\nResults: False - already in section of 116", manager.enrollStudentInCourse(catalog.getProductFromCatalog("CSC216", "601")));
	    assertFalse("Action: enroll\nUser: ahicks\nCourse: CSC216-001, CSC226-001, CSC116-003, CSC230-001\nResults: False - exceeded max credits", manager.enrollStudentInCourse(catalog.getProductFromCatalog("CSC230", "001")));
	    
	    Product ahicks = directory.getEmployeeById("ahicks");
	    VendorSchedule scheduleHicks = ahicks.getSchedule();
	    assertEquals(10, scheduleHicks.getScheduleCredits());
	    String[][] scheduleHicksArray = scheduleHicks.getScheduledCourses();
	    assertEquals(3, scheduleHicksArray.length);
	    assertEquals("CSC216", scheduleHicksArray[0][0]);
	    assertEquals("001", scheduleHicksArray[0][1]);
	    assertEquals("Programming Concepts - Java", scheduleHicksArray[0][2]);
	    assertEquals("TH 1:30PM-2:45PM", scheduleHicksArray[0][3]);
	    assertEquals("9", scheduleHicksArray[0][4]);
	    assertEquals("CSC226", scheduleHicksArray[1][0]);
	    assertEquals("001", scheduleHicksArray[1][1]);
	    assertEquals("Discrete Mathematics for Computer Scientists", scheduleHicksArray[1][2]);
	    assertEquals("MWF 9:35AM-10:25AM", scheduleHicksArray[1][3]);
	    assertEquals("9", scheduleHicksArray[1][4]);
	    assertEquals("CSC116", scheduleHicksArray[2][0]);
	    assertEquals("003", scheduleHicksArray[2][1]);
	    assertEquals("Intro to Programming - Java", scheduleHicksArray[2][2]);
	    assertEquals("TH 11:20AM-1:10PM", scheduleHicksArray[2][3]);
	    assertEquals("9", scheduleHicksArray[2][4]);
	    
	    assertTrue("Action: drop\nUser: efrost\nCourse: CSC226-001\nResults: True", manager.dropStudentFromCourse(catalog.getProductFromCatalog("CSC226", "001")));
	    
	    //Check schedule
	    ahicks = directory.getEmployeeById("ahicks");
	    scheduleHicks = ahicks.getSchedule();
	    assertEquals(7, scheduleHicks.getScheduleCredits());
	    scheduleHicksArray = scheduleHicks.getScheduledCourses();
	    assertEquals(2, scheduleHicksArray.length);
	    assertEquals("CSC216", scheduleHicksArray[0][0]);
	    assertEquals("001", scheduleHicksArray[0][1]);
	    assertEquals("Programming Concepts - Java", scheduleHicksArray[0][2]);
	    assertEquals("TH 1:30PM-2:45PM", scheduleHicksArray[0][3]);
	    assertEquals("9", scheduleHicksArray[0][4]);
	    assertEquals("CSC116", scheduleHicksArray[1][0]);
	    assertEquals("003", scheduleHicksArray[1][1]);
	    assertEquals("Intro to Programming - Java", scheduleHicksArray[1][2]);
	    assertEquals("TH 11:20AM-1:10PM", scheduleHicksArray[1][3]);
	    assertEquals("9", scheduleHicksArray[1][4]);
	    
	    assertFalse("Action: drop\nUser: efrost\nCourse: CSC226-001\nResults: False - already dropped", manager.dropStudentFromCourse(catalog.getProductFromCatalog("CSC226", "001")));
	    
	    assertTrue("Action: drop\nUser: efrost\nCourse: CSC216-001\nResults: True", manager.dropStudentFromCourse(catalog.getProductFromCatalog("CSC216", "001")));
	    
	    //Check schedule
	    ahicks = directory.getEmployeeById("ahicks");
	    scheduleHicks = ahicks.getSchedule();
	    assertEquals(3, scheduleHicks.getScheduleCredits());
	    scheduleHicksArray = scheduleHicks.getScheduledCourses();
	    assertEquals(1, scheduleHicksArray.length);
	    assertEquals("CSC116", scheduleHicksArray[0][0]);
	    assertEquals("003", scheduleHicksArray[0][1]);
	    assertEquals("Intro to Programming - Java", scheduleHicksArray[0][2]);
	    assertEquals("TH 11:20AM-1:10PM", scheduleHicksArray[0][3]);
	    assertEquals("9", scheduleHicksArray[0][4]);
	    
	    assertTrue("Action: drop\nUser: efrost\nCourse: CSC116-003\nResults: True", manager.dropStudentFromCourse(catalog.getProductFromCatalog("CSC116", "003")));
	    
	    //Check schedule
	    ahicks = directory.getEmployeeById("ahicks");
	    scheduleHicks = ahicks.getSchedule();
	    assertEquals(0, scheduleHicks.getScheduleCredits());
	    scheduleHicksArray = scheduleHicks.getScheduledCourses();
	    assertEquals(0, scheduleHicksArray.length);
	    
	    manager.logout();
	}

	/**
	 * Tests RegistrationManager.resetSchedule()
	 */
	@Test
	public void testResetSchedule() {
	    directory = manager.getVendorDirectory();
	    directory.loadStudentsFromFile("test-files/student_records.txt");
	    
	    catalog = manager.getNioxCatalog();
	    catalog.loadProductsFromFile("test-files/course_records.txt");
	    
	    manager.logout(); //In case not handled elsewhere
	    
	    //Test if not logged in
	    try {
	        manager.resetSchedule();
	        fail("RegistrationManager.resetSchedule() - If the current user is null, an IllegalArgumentException should be thrown, but was not.");
	    } catch (IllegalArgumentException e) {
	        assertNull("RegistrationManager.resetSchedule() - currentUser is null, so cannot enroll in course.", manager.getCurrentUser());
	    }
	    
	    //test if registrar is logged in
	    manager.login("registrar", "Regi5tr@r");
	    try {
	        manager.resetSchedule();
	        fail("RegistrationManager.resetSchedule() - If the current user is registrar, an IllegalArgumentException should be thrown, but was not.");
	    } catch (IllegalArgumentException e) {
	        assertEquals("RegistrationManager.resetSchedule() - currentUser is registrar, so cannot enroll in course.", "registrar", manager.getCurrentUser().getId());
	    }
	    manager.logout();
	    
	    manager.login("efrost", "pw");
	    assertFalse("Action: enroll\nUser: efrost\nCourse: CSC216-001\nResults: False - Student max credits are 3 and course has 4.", manager.enrollStudentInCourse(catalog.getProductFromCatalog("CSC216", "001")));
	    assertTrue("Action: enroll\nUser: efrost\nCourse: CSC226-001\nResults: True", manager.enrollStudentInCourse(catalog.getProductFromCatalog("CSC226", "001")));
	    assertFalse("Action: enroll\nUser: efrost\nCourse: CSC226-001, CSC230-001\nResults: False - cannot exceed max student credits.", manager.enrollStudentInCourse(catalog.getProductFromCatalog("CSC230", "001")));
	    
	    manager.resetSchedule();
	    //Check Student Schedule
	    Product efrost = directory.getEmployeeById("efrost");
	    VendorSchedule scheduleFrost = efrost.getSchedule();
	    assertEquals(0, scheduleFrost.getScheduleCredits());
	    String[][] scheduleFrostArray = scheduleFrost.getScheduledCourses();
	    assertEquals(0, scheduleFrostArray.length);
	    
	    manager.logout();
	    
	    manager.login("ahicks", "pw");
	    assertTrue("Action: enroll\nUser: ahicks\nCourse: CSC216-001\nResults: True", manager.enrollStudentInCourse(catalog.getProductFromCatalog("CSC216", "001")));
	    assertTrue("Action: enroll\nUser: ahicks\nCourse: CSC216-001, CSC226-001\nResults: True", manager.enrollStudentInCourse(catalog.getProductFromCatalog("CSC226", "001")));
	    assertFalse("Action: enroll\nUser: ahicks\nCourse: CSC216-001, CSC226-001, CSC226-001\nResults: False - duplicate", manager.enrollStudentInCourse(catalog.getProductFromCatalog("CSC226", "001")));
	    assertFalse("Action: enroll\nUser: ahicks\nCourse: CSC216-001, CSC226-001, CSC116-001\nResults: False - time conflict", manager.enrollStudentInCourse(catalog.getProductFromCatalog("CSC116", "001")));
	    assertTrue("Action: enroll\nUser: ahicks\nCourse: CSC216-001, CSC226-001, CSC116-003\nResults: True", manager.enrollStudentInCourse(catalog.getProductFromCatalog("CSC116", "003")));
	    assertFalse("Action: enroll\nUser: ahicks\nCourse: CSC216-001, CSC226-001, CSC116-003, CSC116-002\nResults: False - already in section of 116", manager.enrollStudentInCourse(catalog.getProductFromCatalog("CSC216", "601")));
	    assertFalse("Action: enroll\nUser: ahicks\nCourse: CSC216-001, CSC226-001, CSC116-003, CSC230-001\nResults: False - exceeded max credits", manager.enrollStudentInCourse(catalog.getProductFromCatalog("CSC230", "001")));
	    
	    manager.resetSchedule();
	    //Check Student schedule
	    Product ahicks = directory.getEmployeeById("ahicks");
	    VendorSchedule scheduleHicks = ahicks.getSchedule();
	    assertEquals(0, scheduleHicks.getScheduleCredits());
	    String[][] scheduleHicksArray = scheduleHicks.getScheduledCourses();
	    assertEquals(0, scheduleHicksArray.length);
	    
	    manager.logout();
	}
	
	/**
	 * tests the RegistrationManager.addFacultyToCourse method
	 */
	@Test
	public void testAddFacultyToCourse(){
	    manager.logout();
		manager.login("registrar", "Regi5tr@r");
	    catalog = manager.getNioxCatalog();
	    fDirectory = manager.getFacultyDirectory();
		catalog.loadProductsFromFile(validCourseRecordsFile);
		fDirectory.loadFacultyFromFile("test-files/faculty_records2.txt");
		manager.removeFacultyFromCourse(catalog.getProductFromCatalog("CSC116", "001"), fDirectory.getEmployeeById("jdyoung2"));
		assertTrue(manager.addFacultyToCourse(catalog.getProductFromCatalog("CSC116", "001"), fDirectory.getEmployeeById("lwalls")));
		assertTrue(manager.addFacultyToCourse(catalog.getProductFromCatalog("CSC216", "001"), fDirectory.getEmployeeById("lwalls")));
		assertFalse(manager.addFacultyToCourse(catalog.getProductFromCatalog("CSC230", "001"), fDirectory.getEmployeeById("lwalls")));
		
	}
	
	/**
	 * test the RegistrationManager.removeFacultyFromCourse
	 */
	@Test
	public void testRemoveFacultyFromCourse() {
		 	fDirectory = manager.getFacultyDirectory();
		    fDirectory.loadFacultyFromFile("test-files/faculty_records.txt");
		    
		    catalog = manager.getNioxCatalog();
		    catalog.loadProductsFromFile("test-files/course_records.txt");
		    
		    manager.logout(); //In case not handled elsewhere
		    
		    //test if not logged in
		    try {
		    	Mino c = catalog.getProductFromCatalog("CSC216", "001");
		    	Employee f = fDirectory.getEmployeeById("lwalls");
		        manager.removeFacultyFromCourse(c, f);
		        fail("RegistrationManager.removeFacultyFromCourse() - If the current user is null, an IllegalArgumentException should be thrown, but was not.");
		    } catch (IllegalArgumentException e) {
		        assertNull("RegistrationManager.removeFacultyFromCourse() - currentUser is null, so cannot enroll in course.", manager.getCurrentUser());
		    }
		    
		    //test if registrar is logged in
		    manager.login("registrar", "Regi5tr@r");
		    try {
		    	Mino c = catalog.getProductFromCatalog("CSC216", "001");
		    	Employee f = fDirectory.getEmployeeById("lwalls");
		        manager.removeFacultyFromCourse(c, f);
		        assertEquals("RegistrationManager.removeFacultyFromCourse() - currentUser is registrar, so cannot enroll in course.", "registrar", manager.getCurrentUser().getId());
		    } catch (IllegalArgumentException e) {
		    	 fail("RegistrationManager.removeFacultyFromCourse() - If the current user is registrar, an IllegalArgumentException should be thrown, but was not.");
		        assertEquals("RegistrationManager.dropStudentFromCourse() - currentUser is registrar, so cannot enroll in course.", "registrar", manager.getCurrentUser().getId());
		    }
		    manager.logout();
	}
	
	/**
	 * test RegistrationManager.resetFacultySchedule
	 */
	@Test
	public void testResetFacultySchedule() {
		 fDirectory = manager.getFacultyDirectory();
		 fDirectory.loadFacultyFromFile("test-files/faculty_records.txt");
		    
		 catalog = manager.getNioxCatalog();
		 catalog.loadProductsFromFile("test-files/course_records.txt");
		 
		 Mino c = catalog.getProductFromCatalog("CSC216", "001");
	     Employee f = fDirectory.getEmployeeById("lwalls");
		    
		    manager.logout(); //In case not handled elsewhere
		    
		    
		    //Test if not logged in
		    try {
		        manager.resetSchedule();
		        fail("RegistrationManager.resetSchedule() - If the current user is null, an IllegalArgumentException should be thrown, but was not.");
		    } catch (IllegalArgumentException e) {
		        assertNull("RegistrationManager.resetSchedule() - currentUser is null, so cannot enroll in course.", manager.getCurrentUser());
		    }
		    
		    
		    //login as registrar
		    manager.login("registrar", "Regi5tr@r");
		    try {
		    	manager.addFacultyToCourse(c, f);
		        manager.resetFacultySchedule(f);
		        assertEquals("registrar", manager.getCurrentUser().getId());
		    } catch (IllegalArgumentException e) {
		        fail(); // an illegalArgumentException was thrown and it should not have been
		    }
		    manager.logout();
		    
		    Mino c1 = catalog.getProductFromCatalog("CSC230", "001");
		    Employee f1 = fDirectory.getEmployeeById("bbrewer");
		    
		    
		  //login as registrar
		    manager.login("registrar", "Regi5tr@r");
		    try {
		    	manager.addFacultyToCourse(c1, f1);
		        manager.resetFacultySchedule(f1);
		        assertEquals("registrar", manager.getCurrentUser().getId());
		    } catch (IllegalArgumentException e) {
		        fail(); // an illegalArgumentException was thrown and it should not have been
		    }
		    
		    CustomerSchedule f1Sch = f1.getSchedule();
		    assertEquals(0, f1Sch.getNumScheduledCourses());
		    String[][] schf1Array = f1Sch.getScheduledCourses();
		    assertEquals(0, schf1Array.length);
		    
		    manager.logout();
	}
	
	/**
	 * test RegistrationManager.getFacultyDirectory
	 */
	@Test
	public void testGetFacultyDirectory() {
		fDirectory = manager.getFacultyDirectory();
		fDirectory.loadFacultyFromFile("test-files/faculty_records.txt");
		User f = fDirectory.getEmployeeById("awitt");
		String [][] dir = new String[fDirectory.getFacultyDirectory().length][3];
		for (int i = 0; i < dir.length; i++) {
			dir[i][0] = f.getFirstName();
			dir[i][1] = f.getLastName();
			dir[i][2] = f.getId();
		}
		assertEquals(dir[0][0], f.getFirstName());
		assertEquals(dir[0][1], f.getLastName());
		assertEquals(dir[0][2], f.getId());
		
		
	}
	
	
	
	
}