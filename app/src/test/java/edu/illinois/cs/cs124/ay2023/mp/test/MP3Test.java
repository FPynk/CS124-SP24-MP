package edu.illinois.cs.cs124.ay2023.mp.test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSubstring;
import static com.google.common.truth.Truth.assertWithMessage;
import static edu.illinois.cs.cs124.ay2023.mp.test.helpers.Data.COURSES;
import static edu.illinois.cs.cs124.ay2023.mp.test.helpers.Data.OBJECT_MAPPER;
import static edu.illinois.cs.cs124.ay2023.mp.test.helpers.Data.SUMMARIES;
import static edu.illinois.cs.cs124.ay2023.mp.test.helpers.Data.SUMMARY_COUNT;
import static edu.illinois.cs.cs124.ay2023.mp.test.helpers.Data.nodeToPath;
import static edu.illinois.cs.cs124.ay2023.mp.test.helpers.HTTP.getAPIClient;
import static edu.illinois.cs.cs124.ay2023.mp.test.helpers.HTTP.testClient;
import static edu.illinois.cs.cs124.ay2023.mp.test.helpers.HTTP.testServerGet;
import static edu.illinois.cs.cs124.ay2023.mp.test.helpers.HTTP.testServerGetTimed;
import static edu.illinois.cs.cs124.ay2023.mp.test.helpers.HTTP.testServerPost;
import static edu.illinois.cs.cs124.ay2023.mp.test.helpers.Helpers.configureLogging;
import static edu.illinois.cs.cs124.ay2023.mp.test.helpers.Helpers.pause;
import static edu.illinois.cs.cs124.ay2023.mp.test.helpers.Helpers.startActivity;
import static edu.illinois.cs.cs124.ay2023.mp.test.helpers.Views.hasRating;
import static edu.illinois.cs.cs124.ay2023.mp.test.helpers.Views.setRating;

import android.content.Intent;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * This is the MP3 test suite.
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
 * The MP3 test suite includes no ungraded tests.
 * These tests are fairly idiomatic, in that they resemble tests you might write for an actual
 * Android programming project.
 */

@RunWith(AndroidJUnit4.class)
@LooperMode(LooperMode.Mode.PAUSED)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class MP3Test {
  /////////////////////////////////////////////////////////////////////////////////////////////////
  // Unit tests that don't require simulating the entire app, and usually complete quickly
  /////////////////////////////////////////////////////////////////////////////////////////////////

  /** Test the GET rating server route. */
  @Test(timeout = 10000L)
  @Graded(points = 10, friendlyName = "Server GET /rating/")
  @LazyApplication(LazyApplication.LazyLoad.ON)
  public void test0_ServerGETRating() throws IOException {
    // Note that until you complete the POST /rating/ route, this test is fairly limited
    // Test good requests
    for (String courseString : COURSES) {
      JsonNode node = OBJECT_MAPPER.readTree(courseString);
      Rating rating = testServerGet("/rating/" + nodeToPath(node), Rating.class);

      // Check the rating returned by GET /rating/
      assertWithMessage("Incorrect rating for unrated course")
          .that(rating.getRating())
          .isEqualTo(Rating.NOT_RATED);

      // Check other parts of GET /rating/ response
      assertWithMessage("Incorrect subject for unrated course")
          .that(rating.getSummary().getSubject())
          .isEqualTo(node.get("subject").asText());
      assertWithMessage("Incorrect number for unrated course")
          .that(rating.getSummary().getNumber())
          .isEqualTo(node.get("number").asText());
    }

    // Test bad requests
    // Bad URL
    testServerGet("/rating/CS/", HttpURLConnection.HTTP_BAD_REQUEST);
    // Non-existent course
    testServerGet("/rating/CS/188/", HttpURLConnection.HTTP_NOT_FOUND);
    // Non-existent URL
    testServerGet("/ratings/CS/124/", HttpURLConnection.HTTP_NOT_FOUND);
  }

  /** Test the POST rating server route. */
  @Test(timeout = 20000L)
  @Graded(points = 20, friendlyName = "Server POST /rating/")
  @LazyApplication(LazyApplication.LazyLoad.ON)
  public void test1_ServerPOSTRating() throws IOException {

    // Proceed through courses in a deterministic random order
    Random random = new Random(124);
    List<String> shuffledCourses = new ArrayList<>(COURSES);
    Collections.shuffle(shuffledCourses, random);

    // Perform initial GET /rating/ requests
    for (String courseString : shuffledCourses) {
      // Construct URL
      JsonNode node = OBJECT_MAPPER.readTree(courseString);
      String url = "/rating/" + nodeToPath(node);

      // Perform initial GET
      Rating rating = testServerGet(url, Rating.class);
      assertWithMessage("Incorrect rating for unrated course")
          .that(rating.getRating())
          .isEqualTo(Rating.NOT_RATED);
    }

    // Perform POST /rating/ requests to change ratings
    Map<String, Float> ratings = new HashMap<>();
    Collections.shuffle(shuffledCourses, random);
    for (String courseString : shuffledCourses) {
      JsonNode node = OBJECT_MAPPER.readTree(courseString);

      // POST to change rating
      float testRating = random.nextInt(51) / 10.0f;

      // Construct POST rating body
      ObjectNode newRating = OBJECT_MAPPER.createObjectNode();
      newRating.set("summary", OBJECT_MAPPER.convertValue(node, JsonNode.class));
      newRating.set("rating", OBJECT_MAPPER.convertValue(testRating, JsonNode.class));

      Rating rating = testServerPost("/rating/", newRating, Rating.class);
      assertWithMessage("Incorrect rating from rating POST")
          .that(rating.getRating())
          .isEqualTo(testRating);

      // Save rating value for next stage
      ratings.put(nodeToPath(node), testRating);
    }

    // Save response times for comparison
    List<Long> baseResponseTimes = new ArrayList<>();
    List<Long> getResponseTimes = new ArrayList<>();

    // Second route of GET /rating/ requests to ensure ratings are saved
    Collections.shuffle(shuffledCourses, random);
    for (String courseString : shuffledCourses) {
      // Time the index route for comparison
      HTTP.TimedResponse<Course> baseResult = testServerGetTimed("/");
      baseResponseTimes.add(baseResult.getResponseTime().toNanos());

      // Construct URL
      JsonNode node = OBJECT_MAPPER.readTree(courseString);
      String url = "/rating/" + nodeToPath(node);

      // Retrieve saved rating
      float savedRating = ratings.get(nodeToPath(node));

      // Final GET
      HTTP.TimedResponse<Rating> ratingResult = testServerGetTimed(url, Rating.class);
      assertWithMessage("Incorrect rating for course: should be " + savedRating)
          .that(ratingResult.getResponse().getRating())
          .isEqualTo(savedRating);
      getResponseTimes.add(ratingResult.getResponseTime().toNanos());
    }

    // Check for slow server GETs potentially caused by unnecessary parsing
    double averageBase =
        ((double) baseResponseTimes.stream().reduce(0L, Long::sum)) / baseResponseTimes.size();
    double averageResponse =
        ((double) getResponseTimes.stream().reduce(0L, Long::sum)) / getResponseTimes.size();
    assertWithMessage("Server GET /rating/ is too slow")
        .that(averageResponse)
        .isLessThan(averageBase * Helpers.GET_METHOD_EXTRA_TIME);

    // Bad requests
    Summary testingSummary = new Summary("CS", "124", "");
    ObjectNode newRating = OBJECT_MAPPER.createObjectNode();
    newRating.set("summary", OBJECT_MAPPER.convertValue(testingSummary, JsonNode.class));
    newRating.set("rating", OBJECT_MAPPER.convertValue(3.0, JsonNode.class));

    // Bad URL
    testServerPost("/ratings/", testingSummary, HttpURLConnection.HTTP_NOT_FOUND);

    // Non-existing course in rating
    Summary nonexistentSummary = new Summary("CS", "123", "");
    newRating.set("summary", OBJECT_MAPPER.convertValue(nonexistentSummary, JsonNode.class));
    testServerPost("/rating/", newRating, HttpURLConnection.HTTP_NOT_FOUND);

    // Bad body
    testServerPost("/rating/", "test me", HttpURLConnection.HTTP_BAD_REQUEST);
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

  /** Test the client getRating method */
  @Test(timeout = 20000L)
  @Graded(points = 10, friendlyName = "Client GET /rating/")
  @LazyApplication(LazyApplication.LazyLoad.ON)
  public void test2_ClientGETRating() throws Exception {
    // Note that until you complete the POST /rating/ route, this test is fairly limited
    Client apiClient = getAPIClient();

    // Get rating for most courses
    for (int i = 0; i < SUMMARY_COUNT; i++) {
      // Skip summaries we use for the UI test
      if (SUMMARIES_FOR_UI_TEST.contains(i)) {
        continue;
      }

      Summary summary = SUMMARIES.get(i);
      Rating rating = testClient((callback) -> apiClient.getRating(summary, callback));
      assertWithMessage("Incorrect summary subject for unrated course")
          .that(rating.getSummary().getSubject())
          .isEqualTo(summary.getSubject());
      assertWithMessage("Incorrect summary number for unrated course")
          .that(rating.getSummary().getNumber())
          .isEqualTo(summary.getNumber());
      assertWithMessage("Incorrect rating for unrated course")
          .that(rating.getRating())
          .isEqualTo(Rating.NOT_RATED);
    }

    // Test bad getRating request
    try {
      Rating ignored =
          testClient(
              (callback) -> apiClient.getRating(new Summary("Not", "A", "Course"), callback));
      assertWithMessage("Client GET /rating/ for non-existent course should throw").fail();
    } catch (Exception ignored) {
    }
  }

  /** Test the client postRating method */
  @Test(timeout = 10000L)
  @Graded(points = 20, friendlyName = "Client POST /rating/")
  @LazyApplication(LazyApplication.LazyLoad.ON)
  public void test3_ClientPOSTRating() throws Exception {
    Client apiClient = getAPIClient();

    Random random = new Random(124);
    Map<Summary, Float> testRatings = new HashMap<>();

    // Go through all courses twice
    for (int repeat = 0; repeat < 2; repeat++) {
      for (int i = 0; i < SUMMARY_COUNT; i++) {
        if (SUMMARIES_FOR_UI_TEST.contains(i)) {
          continue;
        }

        Summary summary = SUMMARIES.get(i);
        // Randomly either GET or POST
        Rating rating;
        if (random.nextBoolean()) {
          rating = testClient((callback) -> apiClient.getRating(summary, callback));
        } else {
          float testRating = random.nextInt(51) / 10.0f;
          testRatings.put(summary, testRating);
          rating =
              testClient(
                  (callback) ->
                      getAPIClient().postRating(new Rating(summary, testRating), callback));
        }

        float expectedRating = testRatings.getOrDefault(summary, Rating.NOT_RATED);
        assertWithMessage("Mismatch on rating").that(rating.getRating()).isEqualTo(expectedRating);
        assertWithMessage("Incorrect summary subject")
            .that(rating.getSummary().getSubject())
            .isEqualTo(summary.getSubject());
        assertWithMessage("Incorrect summary number")
            .that(rating.getSummary().getNumber())
            .isEqualTo(summary.getNumber());
      }
    }
  }

  // Helper method for the UI test
  private void ratingViewHelper(int summaryIndex, int startRating, int endRating)
      throws JsonProcessingException {
    // Pull Summary and Course details
    String summaryString = OBJECT_MAPPER.writeValueAsString(SUMMARIES.get(summaryIndex));
    String courseString = COURSES.get(summaryIndex);

    // Prepare the Intent to start the CourseActivity
    Intent intent = new Intent(ApplicationProvider.getApplicationContext(), CourseActivity.class);
    ObjectNode summaryForIntent = (ObjectNode) OBJECT_MAPPER.readTree(summaryString);
    summaryForIntent.remove("description");
    intent.putExtra("summary", summaryForIntent.toString());
    JsonNode course = OBJECT_MAPPER.readTree(courseString);

    // Start the CourseActivity
    startActivity(
        intent,
        activity -> {
          pause();
          // Test again that the title and description are shown
          String title =
              course.get("subject").asText()
                  + " "
                  + course.get("number").asText()
                  + ": "
                  + course.get("label").asText();
          onView(withSubstring(title)).check(matches(isDisplayed()));
          onView(withSubstring(course.get("description").asText())).check(matches(isDisplayed()));

          // Check that the initial rating is correct, change it, and then verify the change
          onView(withId(R.id.rating))
              .check(hasRating(startRating))
              .perform(setRating(endRating))
              .check(hasRating(endRating));
        });
  }

  /** Test rating view. */
  @Test(timeout = 30000L)
  @Graded(points = 20, friendlyName = "Rating View")
  @LazyApplication(LazyApplication.LazyLoad.ON)
  public void test4_RatingView() throws JsonProcessingException {
    // Map to save current ratings
    Map<Integer, Integer> currentRatings = new HashMap<>();
    // Random for repeatable testing
    Random random = new Random(124);

    // Loop through four random courses, setting initial ratings
    for (int i : SUMMARIES_FOR_UI_TEST) {
      int nextRating = random.nextInt(4) + 1;
      currentRatings.put(i, nextRating);
      pause();
      ratingViewHelper(i, 0, nextRating);
      pause();
    }

    Collections.shuffle(SUMMARIES_FOR_UI_TEST);
    // Loop through four random courses, modifying ratings
    for (int i : SUMMARIES_FOR_UI_TEST) {
      int currentRating = currentRatings.get(i);
      int nextRating = random.nextInt(4) + 1;
      currentRatings.put(i, nextRating);
      pause();
      ratingViewHelper(i, currentRating, nextRating);
      pause();
    }
    // Loop through four random courses, modifying ratings again
    for (int i : SUMMARIES_FOR_UI_TEST) {
      int currentRating = currentRatings.get(i);
      int nextRating = random.nextInt(4) + 1;
      currentRatings.put(i, nextRating);
      pause();
      ratingViewHelper(i, currentRating, nextRating);
      pause();
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
    // Reset the server between tests
    try {
      Server.reset();
    } catch (Exception ignored) {
    }
    // Check for extra reads from courses.json
    // 3 is what's expected based on Robolectric initialization
    assertWithMessage("courses.json should only be accessed during server start")
        .that(coursesReadCountSecurityManager.getCoursesReadCount())
        .isAtMost(3);
  }

  // Run after each test completes
  @After
  public void afterTest() {
    // Check for repeated access to courses.json
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

// md5: f263cefd9afe6333cf8315ef81e3f47a // DO NOT REMOVE THIS LINE
