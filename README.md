WallDee: Simple backend for wall displays
=========================================

# Installation

WallDee is a Play 2.1 application. To run it you first have to download the latest release from

http://www.playframework.org

(Currently tested version is 2.1.1, but later might work as well)

Next you just have to clone the repository somewhere and start it like any other Play application:

```
$ git https://github.com/untoldwind/walldee.git
$ cd walldee
$ <path to your play installation>/play start
```

By default the server will start on port 9000, refer to the Play documentation how to change that is necessary.

# Updating / migrating

All versions of Walldee should be compatible with previous versions (so far at least).
Nevertheless, when upgrading to a newer version it is best tu be safe than sorry. In other words you are encouraged
to created a backup first.

Luckily creating a backup is easy: Just copy the walldee.h2.db file to somewhere else. Also note that walldee
itself creates a backup on daily basis.

# CI

Is hosted by [Travis-CI](https://travis-ci.org/untoldwind/walldee)

![Build status](https://api.travis-ci.org/untoldwind/walldee.png)

# Notes

Large parts of the backend still look very very ugly. <do far most eye-candy went into the wall display itself.

# License

This software is licensed under the MIT license: http://opensource.org/licenses/MIT