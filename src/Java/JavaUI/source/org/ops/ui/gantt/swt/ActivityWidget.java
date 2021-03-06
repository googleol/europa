package org.ops.ui.gantt.swt;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.ops.ui.gantt.model.IGanttActivity;

/**
 * A graphic representation of an activity on Gantt chart
 *
 * @author tatiana
 */
public class ActivityWidget extends Figure {
	public static final int halfHeight = 5;

	public static TokenColor DEFAULT_COLOR;

	private final IGanttActivity activity;
	private final GenericGanttView view; // our master, so we can send selection events
	private TokenColor color;

	private void tokenSelected() {
		view.setSelection(new TokenSelection(activity));
	}

	public ActivityWidget(IGanttActivity activity, TokenColor color, GenericGanttView view) {
		this.activity = activity;
		this.color = color;
		this.view = view;

		this.setLayoutManager(null);

		StringBuffer b = new StringBuffer();
		b.append(activity.getText());
		b.append(" s=[").append(activity.getStartMin()).append(",").append(
				str(activity.getStartMax()));
		b.append("] d=[").append(activity.getDurMin()).append(",").append(
				str(activity.getDurMax()));
		b.append("] e=[").append(activity.getEndMin()).append(",").append(
				str(activity.getEndMax()));
		b.append("]");
		this.setToolTip(new Label(b.toString()));
		this.setBackgroundColor(color.body);
		this.setOpaque(true);


		this.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClicked(MouseEvent arg0) {}

			@Override
			public void mousePressed(MouseEvent arg0) {}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				tokenSelected();
			}
		});
	}

	private static String str(int num) {
		if (num == Integer.MAX_VALUE)
			return "inf";
		return String.valueOf(num);
	}

	public void place(int y, int stepSize, int[] hor) {
		int x = Math.max(activity.getStartMin(), hor[0]);
		int w = Math.min(activity.getEndMax(), hor[1]) - x;
		int h = halfHeight * (2 + (TimelinePanel.showDurationLine ? 1 : 0));
		int width = w * stepSize;
		int xx = x * stepSize;
		int yy = y + halfHeight * 2;
		setBounds(new Rectangle(xx, yy, width, h));

		this.removeAll();
		int x0 = activity.getStartMin();
		this.add(new Box(xx, yy, 0, color.start, x0, activity.getStartMax(), width));
		if (TimelinePanel.showDurationLine) {
			// Zero to min is background, min to max is colored, duration beyond
			// max is cleared to background color
			this.add(new Box(xx, yy, halfHeight, color.duration, activity.getDurMin()
					+ x0, activity.getDurMax() + x0, width));
			int dms = activity.getDurMax() + x0; // duration max shifted
			// If max duration is finite, clear the end of the token to
			// background. MAX_INT will overflow into negative numbers
			if (dms > 0 && dms < Integer.MAX_VALUE)
				this
						.add(new Box(xx, yy, halfHeight, this.getParent()
								.getBackgroundColor(), dms, activity
								.getEndMax(), width));
			this.add(new Box(xx, yy, halfHeight * 2, color.end, activity.getEndMin(),
					activity.getEndMax(), width));
		} else
			this.add(new Box(xx, yy, halfHeight, color.end, activity.getEndMin(),
					activity.getEndMax(), width));
	}

	public IGanttActivity getActivity() {
		return this.activity;
	}

	@Override
	public String toString() {
		return activity.getText() + this.getBounds();
	}

	@Override
	public void setBounds(Rectangle rect) {
		super.setBounds(rect);
	}

	private class Box extends Figure {
		Box(int xx, int yy, int y, Color color, int min, int max, int width) {
			this.setLayoutManager(null);

			int tickSize = GenericGanttView.stepSizePx;
			int x1 = (min - activity.getStartMin()) * tickSize;
			int x2 = (max - activity.getStartMin()) * tickSize;

			// Truncate ends
			if (x1 < 0)
				x1 = 0;
			if (x2 < 0 || x2 > width)
				x2 = width;

			this.setBounds(new Rectangle(x1 + xx, y + yy, x2 - x1, halfHeight));
			this.setBackgroundColor(color);
			this.setOpaque(true);
		}
	}
}
