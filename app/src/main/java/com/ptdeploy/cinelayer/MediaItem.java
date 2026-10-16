package com.ptdeploy.cinelayer;

public final class MediaItem {
  public final String uri, name, type;
  public final long duration, added;
  public boolean favorite;
  public final boolean sample;
  public String notes = "", colorTag = "", frameRate = "", aspectRatio = "";

  public MediaItem(
      String uri,
      String name,
      String type,
      long duration,
      long added,
      boolean favorite,
      boolean sample) {
    this.uri = uri;
    this.name = name;
    this.type = type;
    this.duration = duration;
    this.added = added;
    this.favorite = favorite;
    this.sample = sample;
  }
}
