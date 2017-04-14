/*
 * Copyright (C) 2013-2016 Rinde van Lon, iMinds-DistriNet, KU Leuven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.rinde.logistics.pdptw.mas.comm;

import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.geom.Point;

/**
 * Implementations of this interface can participate in auctions.
 * @author Rinde van Lon
 * @param <T> The bid type.
 */
public interface Bidder<T extends Bid<T>> extends Communicator {

  /*
   * Should compute the 'bid value' for the specified {@link Parcel}. It can be
   * assumed that this method is called only once for each {@link Parcel}, the
   * caller is responsible for any caching if necessary.
   * @param p The {@link Parcel} that needs to be handled.
   * @param time The current time.
   */
  // double getBidFor(Parcel p, long time);

  void callForBids(Auctioneer<T> auctioneer, Parcel p, long time);

  void endOfAuction(Auctioneer<T> auctioneer, Parcel p, long auctionStartTime);

  /*
   * When an auction has been won by this {@link Bidder}, the {@link Parcel} is
   * received via this method.
   * @param p The {@link Parcel} that is won.
   */
  void receiveParcel(Auctioneer<T> auctioneer, Parcel p, long auctionStartTime);

  boolean releaseParcel(Parcel p);

  Point getBidderLocation();

}
