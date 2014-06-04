android-snitch
==============
Android Snitch is a comfortable tool for manual application testing.
It allows to make quick bug reports by email or by redmine.
Android Snitch has 2 cases of use:
1. You found a bug, then you press hardware "sound up" button, then Android-Snitch will send screen shot with last logs attached by email or redmine ticket.
2. App crashes, then our crash report activity will be started, so you would be able to send bug report with logs.

Android Snitch injects code at compilation time, so you don't need to do anything exept include this library.
When you are going to production just remove the library from project.

Version 0.4