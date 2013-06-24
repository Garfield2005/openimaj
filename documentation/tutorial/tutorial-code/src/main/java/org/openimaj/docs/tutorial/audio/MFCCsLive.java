/**
 *
 */
package org.openimaj.docs.tutorial.audio;

import java.net.MalformedURLException;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.JavaSoundAudioGrabber;
import org.openimaj.audio.features.MFCC;
import org.openimaj.audio.processor.FixedSizeSampleAudioProcessor;
import org.openimaj.vis.general.BarVisualisation;

/**
 *	Example that shows the extraction of MFCC features from a live audio stream
 *	and displaying the results in a visualisation.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 18 Jun 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class MFCCsLive
{
	/**
	 * 	Main method
	 *	@param args Command-line args (unused)
	 * 	@throws MalformedURLException Will not be thrown
	 */
	public static void main( final String[] args ) throws MalformedURLException
	{
		// Open a URL to the sine wave sweep. If you have downloaded
		// this file you should use a new File(<filename>) here.
		final JavaSoundAudioGrabber jsag = new JavaSoundAudioGrabber(
				new AudioFormat( 16, 44.1, 1 ) );
		new Thread( jsag ).start();

		// Let's create 30ms windows with 10ms overlap
		final FixedSizeSampleAudioProcessor fssap = new FixedSizeSampleAudioProcessor( jsag, 441*3, 441 );

		// Create the Fourier transform processor chained to the audio decoder
		final MFCC mfcc = new MFCC( fssap );

		// Create a visualisation to show our FFT and open the window now
		final BarVisualisation bv = new BarVisualisation( 400, 200 );
		bv.setAxisLocation( 100 );
		bv.showWindow( "MFCCs" );

		// Loop through the sample chunks from the audio capture thread
		// sending each one through the feature extractor and displaying
		// the results in the visualisation.
		while( mfcc.nextSampleChunk() != null )
		{
			final double[][] mfccs = mfcc.getLastCalculatedFeatureWithoutFirst();
			bv.setData( mfccs[0] );
		}
	}
}
