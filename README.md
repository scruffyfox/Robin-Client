# Robin - Android Client

This is the main Android client (Robin) for the App.net service. It was originally created by [Callum Taylor](http://callumtaylor.net) and [Romain Piel](http://romainpiel.com/), with [Damian Gribben](https://twitter.com/simpleline) as designer. Romain left development in the end of 2012 and Callum and Damian continued development until its shutdown in mid 2014.

There are 2 main versions of the app, the "v1" client was the only version to be launched onto Google Play. After moderate success, the app underwent a complete re-write in an attempt to fix the major bugs in the app and revamp the design to comply more with 4.0+ design guidelines. (Techincally v3) version 2 of the app began late 2013 and continued until the shutdown. You can read more about the shutdown [here](https://github.com/scruffyfox/Robin-Blog/blob/master/public/posts/End%20of%20an%20Era.md).

There should be no sensitive data in the source code, but if there is, please let me know!

The code is provided as-is and has not been maintained for 8 months and **will** be incompatible with latest IDEs.

Version 1 of the app is only compatibile with Eclipse, where as version 2 was built using Android Studio version 0.4 and Gradle 0.9.

You **may not** fork the project, rename and sell it as-is. Please don't do this, I put nearly 2 years worth of work into it, at least improve on it, and if you do wish to sell an app based on this code, please think of me and maybe give me something in return.

## Screenshots

[Link to screenshots](https://imgur.com/a/3O5mA)

## Disclaimer

I do not claim for any of this work to be good, or useful, simply open sourcing it in case other people wish to make use of it.

## Open source

I have always wanted to open source the app code, but due to bad git practices, couldn't simply push to GitHub, so instead had to strip out certain parts of the source (passwords/api keys etc) in order to do so. I also wanted to wait a few months before doing so to make sure that there was no chance that I would want to continue development on the app.

The project was super fun to work on and I learned so much, it was such a shame that it didn't work out, but maybe someone will get some good use, or even learn a thing or two, from the codebase.

Some notable mentions in the source code that I'm particularly proud of

1. [Spannable texts](https://github.com/scruffyfox/Robin-Client/tree/master/v2%20client/Robin/src/main/java/in/lib/view/spannable). This was SUPER hard to get right, it basically allows mentions, hashtags, markdown links, etc to be clickable independently in a `TextView`
2. [Code utils](https://github.com/scruffyfox/Robin-Client/blob/master/v2%20client/Robin/src/main/java/in/lib/utils/CodeUtils.java). This was used for the 'custom display name' feature in settings that basically allowed you to customise, to a code level, how usernames are displayed in your feed. This can easily be expanded for dates, post text, etc.
3. [Text Entities](https://github.com/scruffyfox/Robin-Client/tree/master/v2%20client/Robin/src/main/java/in/data/entity). Used for processing mentions, hashtags, and 'post emphasis' allowing text like `**hello**` to be translated into **hello**
4. [The whole adapter system](https://github.com/scruffyfox/Robin-Client/tree/master/v2%20client/Robin/src/main/java/in/controller/adapter). Due to the awesomeness of the App.net API, all the streams were very similar and allowed me to create a single generic adapter which *every* section in the app can use. It also uses a `delegate` style adapter system, outlined in [this blog post](http://antoine-merle.com/blog/2013/06/11/making-a-multiple-view-type-adapter-with-annotations/)
5. [The theming](https://github.com/scruffyfox/Robin-Client/blob/master/v2%20client/Robin/src/main/res/values/themes.xml). Theming is difficult in Android. I made it nice in Robin.

# Links

### Robin blog

Blog rails app for robinapp.net - https://github.com/scruffyfox/Robin-Blog

### Robin client

Android client for app.net called Robin — https://github.com/scruffyfox/Robin-Client

### Robin translation parser

Simple TSV to Strings.xml resource parser - https://github.com/scruffyfox/Robin-TranslationParser

### Robin notification API

Rails API for the app to store notification data into a database for the notification worker - https://github.com/scruffyfox/Robin-NotificationAPI

### Robin notification server

Simple applet that runs on UNIX based servers that uses App.net streaming API and sends push notifications to GCM - https://github.com/scruffyfox/Robin-NotificationWorker

# Footnotes

All projects are GPG signed with my key `60E6 C1E5 939A 8BAD`

It was great working on this project, and so many thanks to all the people that helped along the way, (noteably [Romain Piel](http://romainpiel.com/) and [Damian Gribben](https://twitter.com/simpleline)), those in the beta, giving feedback, sending me beer money (I really did need it at the time to get me through the day), and those who bought the app!

This does mean, however, that the notification server **will** be shutdown, sorry, no more notifications!

## License
Copyright (C) 2012-2014 Callum Taylor, Romain Piel

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

*************

As with the GPL, you may use the code for commercial use, but dont just fork the entire project and re-release it under a different name, that would make you a dick.

If we meet some day, and you find this stuff is worth it, you can buy me a beer in return.

## Warrenty

The code is provided as-is without warrenty. Use and execute at your own risk, please do not contact me about problems.
