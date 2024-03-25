package edu.illinois.cs.cs124.ay2023.mp.test.helpers;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.security.Permission;

/*
 * Used to count the number of times that the API server accesses courses.json, to fail
 * implementations that repeatedly parse the file after Server initialization.
 *
 * Here we utilize the fact that Java's (deprecated) security architecture allows us to record
 * file accesses to courses.json.
 * Normally you'd use this to perform access control, but we can also use it to simply count
 * the number of times a file was accessed.
 */

public class CoursesReadCountSecurityManager extends SecurityManager {
  private final Path coursesPath;
  private int coursesReadCount = 0;

  public CoursesReadCountSecurityManager() {
    try {
      coursesPath =
          Path.of(CoursesReadCountSecurityManager.class.getResource("/courses.json").toURI());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void checkPermission(Permission perm) {}

  @Override
  public void checkPermission(Permission perm, Object context) {}

  @Override
  public void checkRead(String file) {
    if (Path.of(file).equals(coursesPath)) {
      coursesReadCount++;
    }
    super.checkRead(file);
  }

  public void checkRead(String file, Object context) {
    if (Path.of(file).equals(coursesPath)) {
      coursesReadCount++;
    }
    super.checkRead(file, context);
  }

  public int getCoursesReadCount() {
    return coursesReadCount;
  }
}
// md5: c46d6f19ec118ffc29677cf570d86cfb // DO NOT REMOVE THIS LINE
