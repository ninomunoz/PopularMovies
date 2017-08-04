# Popular Movies
Built an app, optimized for tablets, to help users discover popular and highly rated movies on the web. It displays a scrolling grid of movie trailers, launches a details screen whenever a particular movie is selected, allows users to save favorites, play trailers, and read user reviews. This app utilizes core Android user interface components and fetches movie information using themoviedb.org web API.

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
