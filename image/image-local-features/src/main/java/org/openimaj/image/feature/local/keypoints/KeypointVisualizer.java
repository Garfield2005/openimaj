/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.image.feature.local.keypoints;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.image.renderer.ImageRenderer;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Circle;
import org.openimaj.math.geometry.shape.Polygon;


public class KeypointVisualizer<T, Q extends Image<T,Q> & SinglebandImageProcessor.Processable<Float,FImage,Q>> {
	Q image;
	List<? extends Keypoint> keypoints;
	
	public KeypointVisualizer(Q image, List<? extends Keypoint> keys) {
		this.image = image;
		this.keypoints = keys;
	}
	
	public Map<Keypoint, Q> getPatches(int dim) {
		Map<Keypoint, Q> patches = new HashMap<Keypoint, Q>();
		Map<Float, Q> blurred = new HashMap<Float, Q>();
		
		for (Keypoint k : keypoints) {
			//blur image
			if (!blurred.containsKey(k.scale)) {
				blurred.put(k.scale, image.process(new FGaussianConvolve(k.scale)));
			}
			Q blur = blurred.get(k.scale);
			
			//make empty patch
			int sz = (int) (2 * 2 * 3 * k.scale);
			Q patch = image.newInstance(sz, sz);
			
			//extract pixels
			for (int y=0; y<sz; y++) {
				for (int x=0; x<sz; x++) {
					double xbar = x - sz / 2.0;
					double ybar = y - sz / 2.0;
										
					double xx = (xbar * Math.cos(-k.ori) + ybar * Math.sin(-k.ori)) + k.x;
					double yy = (-xbar * Math.sin(-k.ori) + ybar * Math.cos(-k.ori)) + k.y;
					
					patch.setPixel(x, y, blur.getPixelInterp(xx, yy));
				}
			}
			
			patches.put(k, patch.processInline(new ResizeProcessor(dim, dim)));
		}
		
		return patches;
	}
	
	public Q drawPatches(T boxColour, T circleColour) {
		Q output = image.clone();
		ImageRenderer<T, Q> renderer = output.createRenderer();
		
		for (Keypoint k : keypoints) {
			if (boxColour != null) {
				//for (float i=0; i<5; i+=0.1)
				//	output.drawPolygon(getSamplingBox(k,i), col1);
				renderer.drawPolygon(getSamplingBox(k), boxColour);
			}
			
			if (circleColour != null) {
				renderer.drawLine((int)k.x, (int)k.y, -k.ori, (int)k.scale*5, circleColour);
				renderer.drawShape(new Circle(k.x, k.y, k.scale), circleColour);
			}
		}
		
		return output;
	}
	
	public Q drawCenter(T col) {
		Q output = image.clone();
		ImageRenderer<T, Q> renderer = image.createRenderer();
		
		renderer.drawPoints(keypoints, col,2);
		return output;
	}

	public static Polygon getSamplingBox(Keypoint k) {
		return getSamplingBox(k, 0);
	}
	
	public static Polygon getSamplingBox(Keypoint k, float scincr) {
		List<Point2d> vertices = new ArrayList<Point2d>();
		
		vertices.add(new Point2dImpl(k.x - (scincr + 2*3*k.scale), k.y - (scincr + 2*3*k.scale)));
		vertices.add(new Point2dImpl(k.x + (scincr + 2*3*k.scale), k.y - (scincr + 2*3*k.scale)));
		vertices.add(new Point2dImpl(k.x + (scincr + 2*3*k.scale), k.y + (scincr + 2*3*k.scale)));
		vertices.add(new Point2dImpl(k.x - (scincr + 2*3*k.scale), k.y + (scincr + 2*3*k.scale)));

		Polygon poly = new Polygon(vertices);
		
		poly.rotate(new Point2dImpl(k.x, k.y), -k.ori);
		
		return poly;
	}
	
	public static void main(String [] args) throws IOException {
		FImage image = ImageUtilities.readF(KeypointVisualizer.class.getResource("/org/openimaj/image/data/cat.jpg"));
		
		DoGSIFTEngine engine = new DoGSIFTEngine();
		List<Keypoint> keys = engine.findFeatures(image);
		Collections.shuffle(keys);
		keys = keys.subList(0, 50);
		
		System.out.println(keys);
		
		MBFImage rgbimage = new MBFImage(image.clone(), image.clone(), image.clone());
		KeypointVisualizer<Float[], MBFImage> viz = new KeypointVisualizer<Float[], MBFImage>(rgbimage, keys);
		DisplayUtilities.display(viz.drawPatches(RGBColour.RED, RGBColour.GREEN));
		
		ImageUtilities.write(viz.drawPatches(RGBColour.RED, RGBColour.GREEN), new File("/Users/jsh2/Desktop/cat-sift.png"));
	}
}
