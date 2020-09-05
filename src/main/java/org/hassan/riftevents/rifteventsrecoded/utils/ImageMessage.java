package org.hassan.riftevents.rifteventsrecoded.utils;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.hassan.riftevents.rifteventsrecoded.RiftEvents;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class ImageMessage implements Cloneable {
	private final Color[] colors;
	private final String[] lines;
	public BufferedImage getImage;
	public int height;
	public char imgChar;

	public ImageMessage(final BufferedImage image, final int height, final char imgChar) {
		this.colors = new Color[]{new Color(0, 0, 0), new Color(0, 0, 170), new Color(0, 170, 0), new Color(0, 170, 170), new Color(170, 0, 0), new Color(170, 0, 170), new Color(255, 170, 0), new Color(170, 170, 170), new Color(85, 85, 85), new Color(85, 85, 255), new Color(85, 255, 85), new Color(85, 255, 255), new Color(255, 85, 85), new Color(255, 85, 255), new Color(255, 255, 85), new Color(255, 255, 255)};
		final ChatColor[][] chatColors = this.toChatColorArray(image, height);
		this.lines = this.toImgMessage(chatColors, imgChar);
		this.getImage = image;
		this.height = height;
		this.imgChar = imgChar;
	}


	public ImageMessage appendTextToLine(int line, final String text) {
		if (--line > -1 && line < this.lines.length) {
			final String[] lines = this.lines;
			final int n = line;
			lines[n] = lines[n] + " " + text;
		}
		return this;
	}

	public ImageMessage appendTextToLines(int startingline, final String... text) {
		if (--startingline > -1 && startingline < this.lines.length) {
			for (int x = 0; x < text.length && x < this.lines.length; ++x) {
				this.appendTextToLine(startingline + x + 1, text[x]);
			}
		}
		return this;
	}

	public ImageMessage appendTextToLine(int line, final TextComponent text) {
		if (--line > -1 && line < this.lines.length) {
			final String[] lines = this.lines;
			final int n = line;
			lines[n] = lines[n] + " " + text;
		}
		return this;
	}

	public ImageMessage appendTextToLines(int startingline, final TextComponent... text) {
		if (--startingline > -1 && startingline < this.lines.length) {
			for (int x = 0; x < text.length && x < this.lines.length; ++x) {
				this.appendTextToLine(startingline + x + 1, text[x]);
			}
		}
		return this;
	}

	public ImageMessage appendCenteredText(final String... text) {
		for (int y = 0; y < this.lines.length; ++y) {
			if (text.length <= y) {
				return this;
			}
			final int len = 65 - this.lines[y].length();
			this.lines[y] = String.valueOf(this.lines[y]) + this.center(text[y], len);
		}
		return this;
	}

	private ChatColor[][] toChatColorArray(final BufferedImage image, final int height) {
		final double ratio = image.getHeight() / (double) image.getWidth();
		int width = (int) (height / ratio);
		if (width > 10) {
			width = 10;
		}
		final BufferedImage resized = this.resizeImage(image, (int) (height / ratio), height);
		final ChatColor[][] chatImg = new ChatColor[resized.getWidth()][resized.getHeight()];
		for (int x = 0; x < resized.getWidth(); ++x) {
			for (int y = 0; y < resized.getHeight(); ++y) {
				final int rgb = resized.getRGB(x, y);
				final ChatColor closest = this.getClosestChatColor(new Color(rgb, true));
				chatImg[x][y] = closest;
			}
		}
		return chatImg;
	}

	private String[] toImgMessage(final ChatColor[][] colors, final char imgchar) {
		final String[] lines = new String[colors[0].length];
		for (int y = 0; y < colors[0].length; ++y) {
			String line = "";
			for (int x = 0; x < colors.length; ++x) {
				final ChatColor color = colors[x][y];
				line = String.valueOf(line) + ((color != null) ? (String.valueOf(colors[x][y].toString()) + imgchar) : Character.valueOf(' '));
			}
			lines[y] = String.valueOf(line) + ChatColor.RESET;
		}
		return lines;
	}

	private BufferedImage resizeImage(final BufferedImage originalImage, final int width, final int height) {
		final AffineTransform af = new AffineTransform();
		af.scale(width / (double) originalImage.getWidth(), height / (double) originalImage.getHeight());
		final AffineTransformOp operation = new AffineTransformOp(af, 1);
		return operation.filter(originalImage, null);
	}

	private double getDistance(final Color c1, final Color c2) {
		final double rmean = (c1.getRed() + c2.getRed()) / 2.0;
		final double r = c1.getRed() - c2.getRed();
		final double g = c1.getGreen() - c2.getGreen();
		final int b = c1.getBlue() - c2.getBlue();
		final double weightR = 2.0 + rmean / 256.0;
		final double weightG = 4.0;
		final double weightB = 2.0 + (255.0 - rmean) / 256.0;
		return weightR * r * r + weightG * g * g + weightB * b * b;
	}

	private boolean areIdentical(final Color c1, final Color c2) {
		return Math.abs(c1.getRed() - c2.getRed()) <= 5 && Math.abs(c1.getGreen() - c2.getGreen()) <= 5 && Math.abs(c1.getBlue() - c2.getBlue()) <= 5;
	}

	private ChatColor getClosestChatColor(final Color color) {
		if (color.getAlpha() < 128) {
			return null;
		}
		int index = 0;
		double best = -1.0;
		for (int i = 0; i < this.colors.length; ++i) {
			if (this.areIdentical(this.colors[i], color)) {
				return ChatColor.values()[i];
			}
		}
		for (int i = 0; i < this.colors.length; ++i) {
			final double distance = this.getDistance(color, this.colors[i]);
			if (distance < best || best == -1.0) {
				best = distance;
				index = i;
			}
		}
		return ChatColor.values()[index];
	}

	private String center(final String s, final int length) {
		if (s.length() > length) {
			return s.substring(0, length);
		}
		if (s.length() == length) {
			return s;
		}
		final int leftPadding = (length - s.length()) / 2;
		final StringBuilder leftBuilder = new StringBuilder();
		for (int i = 0; i < leftPadding; ++i) {
			leftBuilder.append(" ");
		}
		return leftBuilder.toString() + s;
	}

	public String[] getLines() {
		return this.lines;
	}

	public void sendToPlayer(String game) {
		String[] lines;
		for (int length = (lines = this.lines).length, i = 0; i < length; ++i) {
			final String line = lines[i];
			TextComponent message = new TextComponent(ChatColor.translateAlternateColorCodes('&', line));
			if (line.contains("Clic")) {
				message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Common.colorMessage(RiftEvents.getInstance().getConfig().getString("broadcasts.line-3"))).create()));
				message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/events join " + game));
			}
			Bukkit.getServer().spigot().broadcast(message);
		}
	}

	public ImageMessage clone() {
		try {
			return (ImageMessage) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new Error(e);
		}
	}
}

