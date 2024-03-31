package edu.illinois.cs.cs124.ay2023.mp.test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withSubstring;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.google.common.truth.Truth.assertWithMessage;
import static edu.illinois.cs.cs124.ay2023.mp.test.helpers.Data.COURSES;
import static edu.illinois.cs.cs124.ay2023.mp.test.helpers.Data.OBJECT_MAPPER;
import static edu.illinois.cs.cs124.ay2023.mp.test.helpers.Data.SUMMARIES;
import static edu.illinois.cs.cs124.ay2023.mp.test.helpers.Data.SUMMARY_COUNT;
import static edu.illinois.cs.cs124.ay2023.mp.test.helpers.Data.compareCourses;
import static edu.illinois.cs.cs124.ay2023.mp.test.helpers.Data.nodeToPath;
import static edu.illinois.cs.cs124.ay2023.mp.test.helpers.HTTP.getAPIClient;
import static edu.illinois.cs.cs124.ay2023.mp.test.helpers.HTTP.testClient;
import static edu.illinois.cs.cs124.ay2023.mp.test.helpers.HTTP.testServerGet;
import static edu.illinois.cs.cs124.ay2023.mp.test.helpers.HTTP.testServerGetTimed;
import static edu.illinois.cs.cs124.ay2023.mp.test.helpers.Helpers.configureLogging;
import static edu.illinois.cs.cs124.ay2023.mp.test.helpers.Helpers.pause;
import static edu.illinois.cs.cs124.ay2023.mp.test.helpers.Helpers.startActivity;
import static edu.illinois.cs.cs124.ay2023.mp.test.helpers.Helpers.startMainActivity;
import static edu.illinois.cs.cs124.ay2023.mp.test.helpers.RecyclerViewMatcher.withRecyclerView;
import static edu.illinois.cs.cs124.ay2023.mp.test.helpers.Views.countRecyclerView;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import edu.illinois.cs.cs124.ay2023.mp.R;
import edu.illinois.cs.cs124.ay2023.mp.activities.CourseActivity;
import edu.illinois.cs.cs124.ay2023.mp.models.Course;
import edu.illinois.cs.cs124.ay2023.mp.models.Summary;
import edu.illinois.cs.cs124.ay2023.mp.network.Client;
import edu.illinois.cs.cs124.ay2023.mp.network.Server;
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.CoursesReadCountSecurityManager;
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.HTTP;
import edu.illinois.cs.cs124.ay2023.mp.test.helpers.Helpers;
import edu.illinois.cs.cs125.gradlegrader.annotations.Graded;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.experimental.LazyApplication;

/*
 * This is the MP2 test suite.
 * The code below is used to evaluate your app during testing, local grading, and official grading.
 * You may not understand all of the code below, but you'll need to have some understanding of how
 * it works so that you can determine what is wrong with your app and what you need to fix.
 *
 * ALL CHANGES TO THIS FILE WILL BE OVERWRITTEN DURING OFFICIAL GRADING.
 * You can and should modify the code below if it is useful during your own local testing,
 * but any changes you make will be discarded during official grading.
 * The local grader will not run if the test suites have been modified, so you'll need to undo any
 * local changes before you run the grader.
 *
 * Note that this means that you should not fix problems with the app by modifying the test suites.
 * The test suites are always considered to be correct.
 *
 * Our test suites are broken into two parts.
 * The unit tests are tests that we can perform without running your app.
 * They test things like whether a method works properly or the behavior of your API server.
 * Unit tests are usually fairly fast.
 *
 * The integration tests are tests that require simulating your app.
 * This allows us to test things like your API client, and higher-level aspects of your app's
 * behavior, such as whether it displays the right thing on the display.
 * Because integration tests require simulating your app, they run more slowly.
 *
 * The MP2 test suite includes no ungraded tests.
 * These tests are fairly idiomatic, in that they resemble tests you might write for an actual
 * Android programming project.
 */

@RunWith(AndroidJUnit4.class)
@LooperMode(LooperMode.Mode.PAUSED)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class MP2Test {
  /////////////////////////////////////////////////////////////////////////////////////////////////
  // Unit tests that don't require simulating the entire app, and usually complete quickly
  /////////////////////////////////////////////////////////////////////////////////////////////////

  /** Test the Course class. */
  @Test(timeout = 1000L)
  @Graded(points = 10, friendlyName = "Course Class Design")
  @LazyApplication(LazyApplication.LazyLoad.ON)
  public void test0_CourseClassDesign() throws JsonProcessingException {
    // Test all courses
    for (String expectedString : COURSES) {
      Course course = OBJECT_MAPPER.readValue(expectedString, Course.class);
      String courseString = OBJECT_MAPPER.writeValueAsString(course);
      compareCourses(expectedString, courseString);
    }
  }

  /** Test GET /course/ server route. */
  @Test(timeout = 10000L)
  @Graded(points = 20, friendlyName = "Server GET /course/")
  @LazyApplication(LazyApplication.LazyLoad.ON)
  public void test1_ServerCourseRoute() throws IOException {
    // Save response times for comparison
    List<Long> baseResponseTimes = new ArrayList<>();
    List<Long> getResponseTimes = new ArrayList<>();

    // Test good GET /course/ requests for all courses
    for (String expectedString : COURSES) {
      // Time the index route for comparison
      HTTP.TimedResponse<Course> baseResult = testServerGetTimed("/");
      baseResponseTimes.add(baseResult.getResponseTime().toNanos());

      // Perform the main course GET
      JsonNode node = OBJECT_MAPPER.readTree(expectedString);
      HTTP.TimedResponse<Course> courseResult =
          testServerGetTimed("/course/" + nodeToPath(node), Course.class);
      String courseString = OBJECT_MAPPER.writeValueAsString(courseResult.getResponse());
      compareCourses(expectedString, courseString);
      getResponseTimes.add(courseResult.getResponseTime().toNanos());
    }

    // Check for slow server GETs potentially caused by unnecessary parsing or looping
    double averageBase =
        ((double) baseResponseTimes.stream().reduce(0L, Long::sum)) / baseResponseTimes.size();
    double averageResponse =
        ((double) getResponseTimes.stream().reduce(0L, Long::sum)) / getResponseTimes.size();
    assertWithMessage("Server GET /course/ is too slow")
        .that(averageResponse)
        .isLessThan(averageBase * Helpers.GET_METHOD_EXTRA_TIME);

    // Test bad requests
    // Bad URL
    testServerGet("/course/CS/", HttpURLConnection.HTTP_BAD_REQUEST);
    // Non-existent course
    testServerGet("/course/CS/188/", HttpURLConnection.HTTP_NOT_FOUND);
    // Non-existent URL
    testServerGet("/courses/CS/124/", HttpURLConnection.HTTP_NOT_FOUND);
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////
  // Integration tests that require simulating the entire app, and are usually slower
  /////////////////////////////////////////////////////////////////////////////////////////////////

  // Indices of summaries to use for the UI test
  private static final List<Integer> SUMMARIES_FOR_UI_TEST;

  static {
    // This is remarkably hard in Java
    List<Integer> summaryIndices =
        IntStream.range(0, SUMMARY_COUNT).boxed().collect(Collectors.toList());
    Collections.shuffle(summaryIndices, new Random(124));
    SUMMARIES_FOR_UI_TEST = summaryIndices.subList(0, 4);
  }

  /** Test the client getCourse method. */
  @Test(timeout = 20000L)
  @Graded(points = 10, friendlyName = "Client GET /course/")
  @LazyApplication(LazyApplication.LazyLoad.ON)
  public void test2_ClientGetCourse() throws Exception {
    Client apiClient = getAPIClient();
    // Test getCourse requests for most courses
    for (int i = 0; i < SUMMARY_COUNT; i++) {
      // Skip course summaries we use for the UI test
      if (SUMMARIES_FOR_UI_TEST.contains(i)) {
        continue;
      }
      Summary summary = SUMMARIES.get(i);
      String expectedString = COURSES.get(i);
      Course course = testClient((callback) -> apiClient.getCourse(summary, callback));
      compareCourses(expectedString, OBJECT_MAPPER.writeValueAsString(course));
    }

    // Test bad getCourse request
    try {
      Course ignored =
          testClient(
              (callback) -> apiClient.getCourse(new Summary("Not", "A", "Course"), callback));
      assertWithMessage("Client GET /course/ for non-existent course should throw").fail();
    } catch (Exception ignored) {
    }
  }

  /** Test onClick intent generation in MainActivity. */
  @Test(timeout = 10000L)
  @Graded(points = 20, friendlyName = "Summary Click Launch")
  @LazyApplication(LazyApplication.LazyLoad.ON)
  public void test3_SummaryClickLaunch() {
    startMainActivity(
        activity -> {
          // Sanity checks
          onView(ViewMatchers.withId(R.id.recycler_view)).check(countRecyclerView(SUMMARY_COUNT));
          onView(withRecyclerView(R.id.recycler_view).atPosition(0))
              .check(matches(hasDescendant(withText("CS 100: Computer Science Orientation"))));

          // Perform the click
          onView(withRecyclerView(R.id.recycler_view).atPosition(0)).perform(click());

          // Make sure the intent generated is correct
          String courseExtra =
              shadowOf(activity).getNextStartedActivity().getStringExtra("summary");

          // Need to wrap the checked exception since we're in a callback
          try {
            JsonNode node = OBJECT_MAPPER.readTree(courseExtra);
            assertWithMessage("Intent contains incorrect subject")
                .that(node.get("subject").asText())
                .isEqualTo("CS");
            assertWithMessage("Intent contains incorrect number")
                .that(node.get("number").asText())
                .isEqualTo("100");
            assertWithMessage("Intent contains incorrect label")
                .that(node.get("label").asText())
                .isEqualTo("Computer Science Orientation");
            assertWithMessage("Intent should not contain description")
                .that(node.get("description"))
                .isNull();
            assertWithMessage("Intent contains extra fields").that(node.size()).isEqualTo(3);
          } catch (JsonProcessingException e) {
            throw new IllegalStateException(e.getMessage());
          }
        });
  }

  /** Test CourseActivity UI launched via intent. */
  @Test(timeout = 10000L)
  @Graded(points = 20, friendlyName = "Course View")
  @LazyApplication(LazyApplication.LazyLoad.ON)
  public void test4_CourseView() throws JsonProcessingException {
    // Use the four random courses we picked above
    for (int summaryIndex : SUMMARIES_FOR_UI_TEST) {

      // Create the Intent
      String summaryString = OBJECT_MAPPER.writeValueAsString(SUMMARIES.get(summaryIndex));
      Intent intent = new Intent(ApplicationProvider.getApplicationContext(), CourseActivity.class);
      intent.putExtra("summary", summaryString);

      // Start the CourseActivity and ensure that the title and description are shown
      JsonNode course = OBJECT_MAPPER.readTree(COURSES.get(summaryIndex));
      startActivity(
          intent,
          activity -> {
            pause();
            String title =
                course.get("subject").asText()
                    + " "
                    + course.get("number").asText()
                    + ": "
                    + course.get("label").asText();
            onView(withSubstring(title)).check(matches(isDisplayed()));
            onView(withSubstring(course.get("description").asText())).check(matches(isDisplayed()));
          });
    }
  }

  // Security manager used to count access to courses.json
  private static final CoursesReadCountSecurityManager coursesReadCountSecurityManager =
      new CoursesReadCountSecurityManager();

  // Run once before any test in this suite is started
  @BeforeClass
  public static void beforeClass() {
    // Check Server.java for publicly visible members
    doCheckServerDesign();
    // Set up logging so that you can see log output during testing
    configureLogging();
    // Force loads to perform initialization before we start counting
    COURSES.size();
    SUMMARIES.size();
    // Install our security manager that allows counting access to courses.json
    System.setSecurityManager(coursesReadCountSecurityManager);
    // Start the API server
    Server.start();
  }

  // Run once after all tests in this suite are completed
  @AfterClass
  public static void afterClass() {
    // Remove the custom security manager
    System.setSecurityManager(null);
  }

  // Run before each test in the suite starts
  @Before
  public void beforeTest() {
    // Check for extra reads from courses.json
    // 3 is what's expected based on Robolectric initialization
    assertWithMessage("courses.json should only be accessed during server start")
        .that(coursesReadCountSecurityManager.getCoursesReadCount())
        .isAtMost(3);
  }

  // Run after each test completes
  @After
  public void afterTest() {
    // Check for extra reads from courses.json
    // 3 is what's expected based on Robolectric initialization
    assertWithMessage("courses.json should only be accessed once during server start")
        .that(coursesReadCountSecurityManager.getCoursesReadCount())
        .isAtMost(3);
  }

  private static void doCheckServerDesign() {
    // Check for extra public methods, fields, or inner classes
    long nonPrivateMethodCount =
        Arrays.stream(Server.class.getDeclaredMethods())
            .filter(
                method ->
                    !Modifier.isPrivate(method.getModifiers())
                        && !Modifier.isStatic(method.getModifiers())
                        && !method.getName().equals("dispatch"))
            .count();
    long nonPrivateFieldCount =
        Arrays.stream(Server.class.getDeclaredFields())
            .filter(field -> !Modifier.isPrivate(field.getModifiers()))
            .count();
    long nonPrivateClassCount =
        Arrays.stream(Server.class.getDeclaredClasses())
            .filter(klass -> !Modifier.isPrivate(klass.getModifiers()))
            .count();
    assertWithMessage("Server has visible methods, fields, or classes")
        .that(nonPrivateMethodCount + nonPrivateFieldCount + nonPrivateClassCount)
        .isEqualTo(0);
  }
}

// md5: 5d0475fb38ea7972c14756b7f3f551b1 // DO NOT REMOVE THIS LINE
