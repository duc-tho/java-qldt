public enum AppPermission {
  // ROLES
  ROLE_READ("ROLE:READ"),
  ROLE_CREATE("ROLE:CREATE"),
  ROLE_UPDATE("ROLE:UPDATE"),
  ROLE_DELETE("ROLE:DELETE"),

  // USERS
  USER_READ("USER:READ"),
  USER_CREATE("USER:CREATE"),
  USER_UPDATE("USER:UPDATE"),
  USER_DELETE("USER:DELETE");

  private final String code;

  AppPermission(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
