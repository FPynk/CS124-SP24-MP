package edu.illinois.cs.cs124.ay2023.mp.models;

import androidx.annotation.NonNull;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Model holding the course summary information shown in the summary list.
 *
 * @noinspection unused
 */
public class Summary implements Comparable<Summary> {
  private String subject;

  /**
   * Get the subject for this Summary.
   *
   * @return the subject for this Summary
   */
  @NotNull
  public final String getSubject() {
    return subject;
  }

  private String number;

  /**
   * Get the number for this Summary.
   *
   * @return the number for this Summary
   */
  @NotNull
  public final String getNumber() {
    return number;
  }

  private String label;

  /**
   * Get the label for this Summary.
   *
   * @return the label for this Summary
   */
  @NotNull
  public final String getLabel() {
    return label;
  }

  /** Create an empty Summary. */
  public Summary() {}

  /**
   * Create a Summary with the provided fields.
   *
   * @param setSubject the department for this Summary
   * @param setNumber the number for this Summary
   * @param setLabel the label for this Summary
   */
  public Summary(@NonNull String setSubject, @NonNull String setNumber, @NotNull String setLabel) {
    subject = setSubject;
    number = setNumber;
    label = setLabel;
  }

  /** {@inheritDoc} */
  @NonNull
  @Override
  public String toString() {
    return subject + " " + number + ": " + label;
  }

  @Override
  public int compareTo(Summary o) {
    // Compare by number (as integers)
    int numberComparison = this.number.compareTo(o.number);
    if (numberComparison != 0) {
      return numberComparison;
    }

    // Compare by subject (entire string)
    int subjectComparison = this.subject.compareTo(o.subject);
    System.out.println("Subject comparison result: " + subjectComparison);
    if (subjectComparison != 0) {
      return subjectComparison;
    }
    return 0;
  }

  public static List<Summary> filter(List<Summary> list, String filter) {
    // output list
    List<Summary> filteredList = new ArrayList<>();

    // lowercase and trimming
    String lowerCaseFilter = filter.toLowerCase().trim();

    // Loop through list.
    for (Summary summary : list) {
      // convert summary to string to lowercase and check if they contain filter
      if (summary.toString().toLowerCase().contains(lowerCaseFilter)) {
        // if yes, add to filtered list.
        filteredList.add(summary);
      }
    }
    // natural sort using our compareTo function
    Collections.sort(filteredList);

    // sort by index of the first occurrence of the filter string
    // basically if you see it first then its first
    // if both are at the same index then just use natural sort
    filteredList.sort(new Comparator<Summary>() {
      @Override
      public int compare(Summary o1, Summary o2) {
        int index1 = o1.toString().toLowerCase().indexOf(lowerCaseFilter);
        int index2 = o2.toString().toLowerCase().indexOf(lowerCaseFilter);

        // If both indices are the same, use natural ordering for those elements
        if (index1 == index2) {
          return o1.compareTo(o2);
        }

        return Integer.compare(index1, index2);
      }
    });

    return filteredList;
  }
}
