package com.nhom11.qldt.feature.dashboard.model;

public enum DashboardActionType {
  USERS("dashboard.action.users.title", "dashboard.action.users.description", "/nguoi-dung", "bi:people"),
  DEPARTMENTS("dashboard.action.departments.title", "dashboard.action.departments.description", "/bo-mon", "bi:diagram-3"),
  PERIODS("dashboard.action.periods.title", "dashboard.action.periods.description", "/dot", "bi:calendar-range"),
  TOPIC_APPROVAL("work.topic-approval.title", "dashboard.action.topic-approval.description", "/phe-duyet-de-tai", "bi:journal-check"),
  COUNCILS("work.council.title", "dashboard.action.councils.description", "/hoi-dong", "bi:people-fill"),
  PUBLICATION("work.publication.title", "dashboard.action.publication.description", "/cong-bo-diem", "bi:award"),
  REGISTRATION_APPROVAL("work.registration.title", "dashboard.action.registration-approval.description", "/phe-duyet-dang-ky", "bi:clipboard-check"),
  TOPIC_PROPOSALS("navigation.item.topic-proposals", "dashboard.action.topic-proposals.description", "/de-xuat-de-tai", "bi:journal-plus"),
  EVALUATIONS("work.evaluation.title", "dashboard.action.evaluations.description", "/cham-diem", "bi:clipboard2-check"),
  GROUPS_IN_PROGRESS("work.groups.in-progress", "dashboard.action.groups-in-progress.description", "/nhom-cua-toi", "bi:people"),
  GROUPS_WAITING("work.groups.waiting", "dashboard.action.groups-waiting.description", "/nhom-cua-toi", "bi:hourglass-split"),
  GROUPS_PUBLISHED("work.groups.published", "dashboard.action.groups-published.description", "/nhom-cua-toi", "bi:patch-check");

  private final String titleKey;
  private final String descriptionKey;
  private final String url;
  private final String icon;

  DashboardActionType(String titleKey, String descriptionKey, String url, String icon) {
    this.titleKey = titleKey;
    this.descriptionKey = descriptionKey;
    this.url = url;
    this.icon = icon;
  }

  public String getTitleKey() {
    return titleKey;
  }

  public String getDescriptionKey() {
    return descriptionKey;
  }

  public String getUrl() {
    return url;
  }

  public String getIcon() {
    return icon;
  }
}
