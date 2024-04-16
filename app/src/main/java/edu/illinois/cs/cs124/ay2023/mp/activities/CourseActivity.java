package edu.illinois.cs.cs124.ay2023.mp.activities;
import static edu.illinois.cs.cs124.ay2023.mp.helpers.Helpers.OBJECT_MAPPER;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import edu.illinois.cs.cs124.ay2023.mp.R;
import edu.illinois.cs.cs124.ay2023.mp.application.CourseableApplication;
import edu.illinois.cs.cs124.ay2023.mp.models.Course;
import edu.illinois.cs.cs124.ay2023.mp.models.Rating;
import edu.illinois.cs.cs124.ay2023.mp.models.Summary;

public class CourseActivity extends AppCompatActivity
    implements RatingBar.OnRatingBarChangeListener {
  @SuppressWarnings("unused")
  private static final String TAG = CourseActivity.class.getSimpleName();
  @Override
  protected void onCreate(@Nullable Bundle unused) {
    super.onCreate(unused);

    // Load this activity's layout and set the title
    setContentView(R.layout.activity_course);

    // Set up our UI
    TextView titleTextView = findViewById(R.id.title);
    TextView descriptionTextView = findViewById(R.id.description);
    RatingBar ratingBar = findViewById(R.id.rating);

    // Retrieve the Intent that started this activity and extract the summary data
    Intent intent = getIntent();
    try {
      // Deserialize the summary JSON to a Summary object
      String summaryJson = intent.getStringExtra("summary");
      Summary summary = OBJECT_MAPPER.readValue(summaryJson, Summary.class);

      // Set the title
      String title = summary.getSubject() + " "
          + summary.getNumber()
          + ": " + summary.getLabel();
      runOnUiThread(() -> titleTextView.setText(title));

      // Use the client to get course details based on the summary
      CourseableApplication application = (CourseableApplication) getApplication();
      application.getClient().getCourse(summary, result -> {
        try {
          // Get the course object from the result
          Course course = result.getValue();
          // Update the UI with details about the course
          runOnUiThread(() -> descriptionTextView.setText(course.getDescription()));
        } catch (Exception e) {
          e.printStackTrace();
        }
      });

      // RATINGS
      application.getClient().getRating(summary, result -> {
        try {
          Rating rating = result.getValue();
          runOnUiThread(() -> ratingBar.setRating(rating.getRating()));
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
      ratingBar.setOnRatingBarChangeListener(this);

    } catch (Exception e) {
      e.printStackTrace();
      // Handle the error scenario, maybe set an error message or finish the activity
    }
  }
  @Override
  public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
    //if (fromUser) {
    CourseableApplication application = (CourseableApplication) getApplication();
    Intent intent = getIntent();
    try {
      // Deserialize the summary JSON to a Summary object
      String summaryJson = intent.getStringExtra("summary");
      Summary summary = OBJECT_MAPPER.readValue(summaryJson, Summary.class);
      Rating newRating = new Rating(summary, rating);
      application.getClient().postRating(newRating, result -> {
        try {
          Rating updatedRating = result.getValue();
          runOnUiThread(() -> ratingBar.setRating(updatedRating.getRating()));
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
    }
    //    } else {
    //      System.out.println("This shouldn't appear");
    //    }
  }
}
