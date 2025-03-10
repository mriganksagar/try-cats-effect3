package com.trycatseffect.utils

import cats.effect.IO

extension[A](io:IO[A])
    def bebug:IO[A] = for {
        a <- io
        t = Thread.currentThread().getName()
        _ = println(s"[$t] $a")
    } yield a
