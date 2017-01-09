# Popular Movies
Udacity Nanodegree: Project 2


## Overview:
This application allows users to discover the most popular movies playing.
Upon launch, the user is presented with a grid arrangement of movie posters. 
Using the sort icon in the action bar, the user can choose to sort movies by
popularity, rating or favorites. The user can tap on a movie poster to see 
movie details. 


## API Key:
An API key for themoviedb.org is required to compile and run this application.
Once you've obtained your key, you will need to add it to app/build.gradle
under buildTypes. See example below, replacing `<YOUR_API_KEY>` with your key.

**app/build.gradle**
```
buildTypes {

  buildTypes.each {
    it.buildConfigField("String", "THEMOVIEDB_API_KEY", "\"<YOUR_API_KEY>\"")
  }

}
```