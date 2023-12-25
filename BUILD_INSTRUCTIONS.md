# How to build this project?

## open-prayer-times
First you have to checkout https://github.com/metinkale38/open-prayer-times and publish it to your local maven repository using `gradlew publishToMavenLocal`.

## secrets.xml
Create file `/features/base/src/main/res/secrets.xml` with following content:

`
<resources>
    <string name="GOOGLE_API_KEY">YOUR_API_KEY</string>
    <string name="IGMG_API_KEY">YOUR_IGMG_KEY</string>
    <string name="LONDON_PRAYER_TIMES_API_KEY">YOUR_API_KEY</string>
</resources>
`

1. `GOOGLE_API_KEY` can be obtained through Google Cloud Platform.
2. `IGMG_API_KEY` can not be officially obtained, you cannot build a version with working IGMG Provider. But since 2023 Diyanet and IGMG have exact the same times, so you actually do not need it.
3. For `LONDON_PRAYER_TIMES_API_KEY` you can check https://www.londonprayertimes.com/api/


## Notes
If you find more steps, which have to be done, please create an Issue or a Pull Request.