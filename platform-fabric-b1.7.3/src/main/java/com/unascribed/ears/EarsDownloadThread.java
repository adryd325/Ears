package com.unascribed.ears;

import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.imageio.ImageIO;

import com.unascribed.ears.legacy.LegacyHelper;
import com.unascribed.ears.common.EarsFeatures;
import com.unascribed.ears.common.Alfalfa;
import com.unascribed.ears.common.EarsCommon;
import com.unascribed.ears.common.debug.EarsLog;
import com.unascribed.ears.common.legacy.AWTEarsImage;

import net.minecraft.client.ImageProcessor;

public class EarsDownloadThread {
	public BufferedImage image;

	public EarsDownloadThread(final String string, final ImageProcessor imageProcessor, BufferedImage image) {
		this.image = image;

		(new Thread(() -> {
			HttpURLConnection imageConnection = null;

			try {
				URL url = new URL(string);
				if (string.startsWith("http://s3.amazonaws.com/MinecraftSkins/") && string.endsWith(".png")) {
					String username = string.substring(39, string.length() - 4);
					String newUrl = LegacyHelper.getSkinUrl(username);
					if (newUrl == null) return;
					url = new URL(newUrl);
				}

				imageConnection = (HttpURLConnection)url.openConnection();
				imageConnection.setDoInput(true);
				imageConnection.setDoOutput(false);
				imageConnection.connect();
				if (imageConnection.getResponseCode() / 100 != 4) {
					BufferedImage rawImage = ImageIO.read(imageConnection.getInputStream());
					Alfalfa alfalfa = EarsCommon.preprocessSkin(new AWTEarsImage(rawImage));
					if (imageProcessor == null) {
						EarsDownloadThread.this.image = rawImage;
					} else {
						EarsDownloadThread.this.image = imageProcessor.process(rawImage);
					}

					EarsLog.debug("Platform:Inject", "Process player skin");
					EarsMod.earsSkinFeatures.put(string, EarsFeatures.detect(new AWTEarsImage(EarsDownloadThread.this.image), alfalfa));

					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return;
			} finally {
				if (imageConnection != null) {
					imageConnection.disconnect();
				}
			}
		})).start();
	}
}
