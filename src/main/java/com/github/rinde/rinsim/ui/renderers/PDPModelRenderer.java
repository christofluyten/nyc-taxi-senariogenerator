/*
 * Copyright (C) 2011-2017 Rinde van Lon, imec-DistriNet, KU Leuven
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
package com.github.rinde.rinsim.ui.renderers;

import com.github.rinde.rinsim.core.model.DependencyProvider;
import com.github.rinde.rinsim.core.model.ModelBuilder.AbstractModelBuilder;
import com.github.rinde.rinsim.core.model.pdp.Depot;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.PDPModel.ParcelState;
import com.github.rinde.rinsim.core.model.pdp.PDPModel.VehicleState;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.ui.renderers.CanvasRenderer.AbstractCanvasRenderer;
import com.google.auto.value.AutoValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author Rinde van Lon
 *
 */
public final class PDPModelRenderer extends AbstractCanvasRenderer {
  private static final RGB LIGHT_GRAY = new RGB(205, 201, 201);
  private static final RGB ORANGE = new RGB(255, 160, 0);
  private static final int OVAL_RADIUS_PX = 5;
  private static final int OVAL_DIAMETER_PX = 2 * OVAL_RADIUS_PX;
  private static final int STATE_TEXT_OFFSET = -20;

  private final Color black;
  private final Color white;
  private final Color lightGray;
  private final Color darkGreen;
  private final Color green;
  private final Color orange;
  private final Color blue;
  private final Color foregroundInfo;
  private final Color backgroundInfo;
  private final PDPModel pdpModel;
  private final RoadModel roadModel;
  private final boolean drawDestLines;
  private final RenderHelper helper;

  PDPModelRenderer(RoadModel rm, PDPModel pm, Device d, boolean lines) {
    roadModel = rm;
    pdpModel = pm;

    helper = new RenderHelper();

    drawDestLines = lines;

    black = d.getSystemColor(SWT.COLOR_BLACK);
    white = d.getSystemColor(SWT.COLOR_WHITE);
    darkGreen = d.getSystemColor(SWT.COLOR_DARK_GREEN);
    green = d.getSystemColor(SWT.COLOR_GREEN);
    blue = d.getSystemColor(SWT.COLOR_BLUE);

    lightGray = new Color(d, LIGHT_GRAY);
    orange = new Color(d, ORANGE);

    foregroundInfo = white;
    backgroundInfo = blue;
  }

  @Override
  public void renderStatic(GC gc, ViewPort vp) {}

  @Override
  public void renderDynamic(GC gc, ViewPort vp, long time) {
    helper.adapt(gc, vp);
    synchronized (pdpModel) {
      final Map<RoadUser, Point> posMap = roadModel.getObjectsAndPositions();
      final Set<Vehicle> vehicles = pdpModel.getVehicles();

      final Set<Depot> depots = roadModel.getObjectsOfType(Depot.class);
      for (final Depot d : depots) {
        helper.setBackgroundSysCol(SWT.COLOR_GRAY);
        helper.fillRect(posMap.get(d), OVAL_RADIUS_PX);
      }

      for (final Vehicle v : vehicles) {
        if (posMap.containsKey(v)) {
          final Point p = posMap.get(v);
          final double size = pdpModel.getContentsSize(v);

          final Collection<Parcel> contents = pdpModel.getContents(v);
          final int x = vp.toCoordX(p.x);
          final int y = vp.toCoordY(p.y);

          if (drawDestLines) {
            gc.setForeground(black);
            for (final Parcel parcel : contents) {
              final Point po = parcel.getDeliveryLocation();
              final int xd = vp.toCoordX(po.x);
              final int yd = vp.toCoordY(po.y);
              if (parcel.getDeliveryTimeWindow().isBeforeStart(time)) {
                gc.setBackground(darkGreen);
              } else if (parcel.getDeliveryTimeWindow().isBeforeEnd(time)) {
                gc.setBackground(green);
              } else {
                gc.setBackground(orange);
              }
              gc.drawLine(x, y, xd, yd);
              gc.fillOval(xd - OVAL_RADIUS_PX, yd - OVAL_RADIUS_PX,
                OVAL_DIAMETER_PX, OVAL_DIAMETER_PX);
              gc.drawOval(xd - OVAL_RADIUS_PX, yd - OVAL_RADIUS_PX,
                OVAL_DIAMETER_PX, OVAL_DIAMETER_PX);
            }
          }
          gc.setBackground(backgroundInfo);
          gc.setForeground(foregroundInfo);
//          final VehicleState state = pdpModel.getVehicleState(v);
//          if (state != VehicleState.IDLE) {
//            gc.drawText(
//              state.toString() + " "
//                + pdpModel.getVehicleActionInfo(v).timeNeeded(),
//              x, y - STATE_TEXT_OFFSET);
//          }
//          gc.drawText(
//            String.format("%d (%1.1f)", pdpModel.getContents(v).size(), size),
//            x,
//            y);
        }
      }

      final Collection<Parcel> parcels = pdpModel.getParcels(
        ParcelState.AVAILABLE, ParcelState.ANNOUNCED);
      for (final Parcel parcel : parcels) {

        final Point p = posMap.get(parcel);
        if (posMap.containsKey(parcel)) {
          final int x = vp.toCoordX(p.x);
          final int y = vp.toCoordY(p.y);
          gc.setForeground(lightGray);
          gc.drawLine(x, y, vp.toCoordX(parcel.getDeliveryLocation().x),
            vp.toCoordY(parcel.getDeliveryLocation().y));

          if (parcel.getPickupTimeWindow().isBeforeStart(time)) {
            gc.setBackground(darkGreen);
          } else if (parcel.getPickupTimeWindow().isBeforeEnd(time)) {
            gc.setBackground(green);
          } else {
            gc.setBackground(orange);
          }
          gc.setForeground(black);
          gc.fillOval(x - OVAL_RADIUS_PX, y - OVAL_RADIUS_PX, OVAL_DIAMETER_PX,
            OVAL_DIAMETER_PX);
//          gc.drawText(parcel.toString(),x-5,y+3);
        }
      }
    }
  }

  /**
   * @return A new {@link Builder} for {@link PDPModelRenderer}.
   */
  public static Builder builder() {
    return Builder.create(false);
  }

  /**
   * Builder for {@link PDPModelRenderer}.
   * @author Rinde van Lon
   */
  @AutoValue
  public abstract static class Builder extends
      AbstractModelBuilder<PDPModelRenderer, Void> {

    private static final long serialVersionUID = 8354062467527222977L;

    Builder() {
      setDependencies(RoadModel.class, PDPModel.class, Device.class);
    }

    abstract boolean drawDestLines();

    /**
     * When called the returned builder will create a {@link PDPModelRenderer}
     * that will draw destination lines from each vehicle towards the
     * destinations of its contents (if any).
     * @return A new builder instance.
     */
    public Builder withDestinationLines() {
      return create(true);
    }

    @Override
    public PDPModelRenderer build(DependencyProvider dependencyProvider) {
      final RoadModel rm = dependencyProvider.get(RoadModel.class);
      final PDPModel pm = dependencyProvider.get(PDPModel.class);
      final Device d = dependencyProvider.get(Device.class);
      return new PDPModelRenderer(rm, pm, d, drawDestLines());
    }

    static Builder create(boolean lines) {
      return new AutoValue_PDPModelRenderer_Builder(lines);
    }
  }
}
