/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2005-2011 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.reporting.engine.classic.core.testsupport.gold;

public class StopWatch
{
  private long elapsedTime;
  private long startTime;
  private boolean started;

  public StopWatch()
  {
  }

  public static StopWatch startNew()
  {
    final StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    return stopWatch;
  }

  public void start()
  {
    if (started)
    {
      return;
    }

    startTime = System.nanoTime();
    started = true;
  }

  public void reset()
  {
    elapsedTime = 0;
    startTime = System.nanoTime();
  }

  public void stop()
  {
    if (started == false)
    {
      return;
    }

    started = false;
    elapsedTime += (System.nanoTime() - startTime);
  }

  public long getElapsedTime()
  {
    if (started)
    {
      return elapsedTime + (System.nanoTime() - startTime);
    }
    return elapsedTime;
  }

  public float getElapsedMilliseconds()
  {
    return getElapsedTime() / 1000000.0f;
  }

  public float getElapsedSeconds()
  {
    return getElapsedTime() / 1000000000.0f;
  }

  public String toString()
  {
    return "StopWatch={elapsedTimeInSeconds=" + getElapsedSeconds() + "}";
  }
}
