 /**
 * AudioManipulation.java
 *
 * Time-stamp: <2017-02-21 14:20:09 rlc3>
 *
 * Defines mixer/effect functions on audio streams
 * Utilises the AudioInputStream class 
 * 
 * To compile: javac -classpath editor.jar:. RunEffects.java
 * To run use: java -classpath editor.jar:. RunEffects
 * 
 */ 

import javax.sound.sampled.*;
import java.io.*;
import java.util.*;

public class AudioManipulation {

/**** echo *****************************************************************/

    public static AudioInputStream echo(AudioInputStream ais, int timeDelay, double fading0, double fading1) {

	byte[] a = null;
	int[] data, ch0, ch1;
	int max;

	try{

	    // AudioInputStream methods 
	    int numChannels     = ais.getFormat().getChannels();
	    int sampleSize 	= ais.getFormat().getSampleSizeInBits();
	    boolean isBigEndian = ais.getFormat().isBigEndian();
	    float sampleRate 	= ais.getFormat().getSampleRate();
	    float frameRate 	= ais.getFormat().getFrameRate();
            int frameSize 	= ais.getFormat().getFrameSize(); 
	    int frameLength 	= (int) ais.getFrameLength();

            // sampleRate = framerate = 44100.0 Hz (playback rate = sampling rate!) 
	    // 1 sec = 1000 millisecs 
	    // calculate delay in frames 
	    int frameDelay = (int) (timeDelay/1000.0 * frameRate);

	    // reset the AudioInputStream (mark goes to the start) 
	    ais.reset();

	    // create a byte array of the right size
    	    // recall the lecture OHP slides .. 
	    a = new byte[(int) frameLength*frameSize];

	    // fill the byte array with the data of the AudioInputStream
	    ais.read(a);

	    // Create an integer array, data, of the right size
	    // only reason to do this is enabling type double mixing calculations  
	    data = new int[a.length/2];

	    // fill the integer array by combining two bytes of the
	    // byte array a into one integer
	    // Bytes HB and LB Big Endian make up one integer 
 	    for (int i=0; i<data.length; ++i) {
		/* First byte is HB (most significant digits) - coerce to 32-bit int */
		// HB =def sign_extend(a[2*i]) from 8 bit byte to 32 bit int 
		int HB = (int) a[2*i];
		/* Second byte is LB (least significant digits) - coerce to 32-bit int */
		// LB =def sign_extend(a[2*i+1]) from 8 bit byte to 32 bit int 
		int LB = (int) a[2*i+1];
		// note that data[i] =def sign_extend(HB.LB) 
		// | : Bool^32 x Bool^32 -----> Bool^32 where Bool = {0, 1} 
		data[i] =  HB << 8 | (LB & 0xff); 
 	    }

	    // split samples into two channels
	    // if both channels are faded by the same factor 
	    // then there is no need to split the channels 
	    ch0 = new int[data.length/2];
	    ch1 = new int[data.length/2];
	    for (int i=0; i<data.length/2; i++) {
		ch0[i] = data[2*i];
		ch1[i] = data[2*i+1];
	    }

	    // Adding a faded copy of the early signal to the later signal
	    // THIS IS THE ECHO !!
	    for (int i=frameDelay; i<ch0.length; ++i) {
		ch0[i] += (int) (ch0[i-frameDelay]*fading0);
		ch1[i] += (int) (ch1[i-frameDelay]*fading1);
	    }

	    // combine the two channels
	    for (int i=0; i<data.length; i+=2) {
		data[i]   = ch0[i/2];
		data[i+1] = ch1[i/2];
	    }  

	    // get the maximum amplitute
	    max=0;
	    for (int i=0; i<data.length; ++i) {
		max=Math.max(max,Math.abs(data[i]));
	    }

            // 16 digit 2s-complement range from -2^15 to +2^15-1 = 256*128-1
	    // therefore we linearly scale data[i] values to lie within this range .. 
	    // .. so that each data[i] has a 16 digit "HB.LB binary representation" 
	    if (max > 256*128 - 1) {
		System.out.println("Sound values are linearly scaled by " + (256.0*128.0-1)/max + 
             " because maximum amplitude is larger than upper boundary of allowed value range."); 
		for (int i=0; i<data.length; ++i) {
		    data[i] = (int) (data[i]*(256.0*128.0-1)/max);
		}
            }

	    // convert the integer array to a byte array 
	    for (int i=0; i<data.length; ++i) {
		a[2*i] 	  = (byte)  ((data[i] >> 8) & 0xff);
		a[2*i+1]  = (byte)         (data[i] & 0xff);
	    }

	} catch(Exception e){
	    System.out.println("Something went wrong");
	    e.printStackTrace();
	}

	// create a new AudioInputStream out of the the byteArray
	// and return it.
	return new AudioInputStream(new ByteArrayInputStream(a),
				    ais.getFormat(),ais.getFrameLength());
    }

/**** scaleToZero *****************************************************************/

    public static AudioInputStream scaleToZero(AudioInputStream ais) {

	byte[] a = null;
	int[] data, ch0, ch1;
	int max = 0;



	try{
	
         // AudioInputStream methods 
	    
        int frameSize 	= ais.getFormat().getFrameSize(); 
	    int frameLength 	= (int) ais.getFrameLength();

	    // reset the AudioInputStream (mark goes to the start) 
	     ais.reset();

	    // create a byte array of the right size
	    a = new byte[(int) frameLength*frameSize];

	    // fill the byte array with the data of the AudioInputStream
	    ais.read(a);

	    // Create an integer array, data, of the right size
	    // only reason to do this is enabling type float/double mixing calculations  
	    data = new int[a.length/2];

	    // fill the integer array by combining two bytes of the
	    // byte array a into one integer - see lectures
	    for (int i=0; i<data.length; ++i) {
		/* First byte is HB (most significant digits) - coerce to 32-bit int */
		// HB =def sign_extend(a[2*i]) from 8 bit byte to 32 bit int 
		int HB = (int) a[2*i];
		/* Second byte is LB (least significant digits) - coerce to 32-bit int */
		// LB =def sign_extend(a[2*i+1]) from 8 bit byte to 32 bit int 
		int LB = (int) a[2*i+1];
		// note that data[i] =def sign_extend(HB.LB) 
		// | : Bool^32 x Bool^32 -----> Bool^32 where Bool = {0, 1} 
		data[i] =  HB << 8 | (LB & 0xff); 
 	    }

	    // scale data linearly to zero 
	    // **** NB this is the only part of scaleToZero that is not already part of
	    // echo effect !!!! ****
	    for(int i = 0; i < data.length; i++){
	    	double scalef = ((double)(data.length-1)-i)/(double)(data.length-1);
	    	data[i] = (int) (scalef*((double)data[i]));
	    }
	    // get the maximum amplitute
	    		max=0;
	    for (int i=0; i<data.length; ++i) {
		max=Math.max(max,Math.abs(data[i]));
	    }


	    // linear scale the maximum amplitude down to abs(-2^15)
	    if (max > 256*128 - 1) {
			System.out.println("Sound values are linearly scaled by " + (256.0*128.0-1)/max + 
	             " because maximum amplitude is larger than upper boundary of allowed value range."); 
			for (int i=0; i<data.length; ++i) {
			    data[i] = (int) (data[i]*(256.0*128.0-1)/max);
			}
	            }

	    // convert the integer array to a byte array 
	    for (int i=0; i<data.length; ++i) {
		a[2*i] 	  = (byte)  ((data[i] >> 8) & 0xff);
		a[2*i+1]  = (byte)         (data[i] & 0xff);
	    }
	  


	} catch(Exception e){
	    System.out.println("Something went wrong");
	    e.printStackTrace();
	}


	// create a new AudioInputStream out of the the byteArray
	// and return it.
	return new AudioInputStream(new ByteArrayInputStream(a),
				    ais.getFormat(),ais.getFrameLength());
    }


/**** addNote *****************************************************************/

    public static AudioInputStream addNote(AudioInputStream ais,
                                           double frequency,
					   int noteLengthInMilliseconds) {
	byte[] a = null;
	int[] data;
	int frameSize 	= ais.getFormat().getFrameSize(); 
	int numChannels = ais.getFormat().getChannels(); 



      try { 
  
	// number of frames for the note of noteLengthInMilliseconds
 	float frameRate = ais.getFormat().getFrameRate();
 	//divide milliseconds by 1000 to get seconds, then multiply by frameRate (frames per second) to get note length in frames
	int noteLengthInFrames = (int) ((noteLengthInMilliseconds/1000.0)*frameRate);
	//multiply by total no of frames to get note length in bytes
	int noteLengthInBytes  = noteLengthInFrames*frameSize;
	int noteLengthInInts   = noteLengthInBytes/2;

	a   = new byte[noteLengthInBytes];
	//creation of a data array to do mixing later on
	data = new int[noteLengthInInts];
	
	for (int i=0; i<noteLengthInInts; i+=2) {
	    	
		
		//amplitude - given in instructions
		int amplitude = 64*256;
		double timeTaken = 1/frameRate;
		int noFrames = i/2;
		double t = timeTaken * noFrames;
		data[i]   = (int) ((int) amplitude * Math.sin(frequency*2*Math.PI*t));
		data[i+1]   = (int) ((int) amplitude * Math.sin(frequency*2*Math.PI*t));
	}

	// copy the int data[i] array into byte a[i] array 			   
	for (int i=0; i<data.length; ++i) {
		a[2*i] 	  = (byte)  ((data[i] >> 8) & 0xff);
		a[2*i+1]  = (byte)         (data[i] & 0xff);
	    } 
	
	} catch(Exception e){
	    System.out.println("Something went wrong");
	    e.printStackTrace();
	}


	return append(new AudioInputStream(new ByteArrayInputStream(a), ais.getFormat(), a.length/ais.getFormat().getFrameSize()),ais);

    }  // end addNote


/**** append *****************************************************************/

      // THIS METHOD append IS SUPPLIED FOR YOU 
    public static AudioInputStream append(AudioInputStream ais1, AudioInputStream ais2){
		
		byte[] a,b,c = null;
		try {
			a=new byte[(int) ais1.getFrameLength() *
			ais1.getFormat().getFrameSize()];

			// fill the byte array with the data of the AudioInputStream
			ais1.read(a);
			b=new byte[(int) ais2.getFrameLength() *
			ais2.getFormat().getFrameSize()];

			// fill the byte array with the data of the AudioInputStream
			ais2.read(b);
			
			c=new byte[a.length + b.length];
			for (int i=0; i<c.length; i++) {
				if (i<a.length) {
					c[i]=a[i];
				}
				else	
					c[i]=b[i-a.length];
			}
		
		} catch(Exception e){
			System.out.println("Something went wrong");
			e.printStackTrace();
		}
			

        return new AudioInputStream(new ByteArrayInputStream(c),
				    ais1.getFormat(), c.length/ais1.getFormat().getFrameSize());
	} // end append

/**** tune  *****************************************************************/

	public static AudioInputStream tune(AudioInputStream ais){

     	//AudioInputStream temp = null;

		byte[] c = new byte[1];
                AudioInputStream temp = new AudioInputStream(new ByteArrayInputStream(c),ais.getFormat(),0);

		// specify variable names for both the frequencies in Hz and note lengths in seconds 
		// eg double C4, D4 etc for frequencies and s, l, ll, lll for lengths 
		// Hint: Each octave results in a doubling of frequency.

		double C4	= 261.63;
		double D4	= 293.66; 	
		double Eb4  = 311.13;
		double E4	= 329.63;
		double E5	= E4*2;
		double E6	= E5*2;
		double F4	= 349.23;
		double F6	= F4*2*2;
		double G4	= 392.00;
		double G6	= G4*2*2;
		double A4	= 440.00;
		double A5	= A4*2;
		double A6 	= A5*2;
		double B4	= 493.88;
		double B5	= B4*2;
		double B6 	= B5*2; 
		double C5	= 523.25;
		double C6 	= C5*2;
		double D5	= 587.33;
		double D6	= D5*2;
		double Eb5  = 622.25;
		double C7	= 2093.00;
		

		// and the lengths in milliseconds
        	int s = 500;
        	int l = 2000;
        	int ll = 2500;
        	int lll = 2800;
		 

		// also sprach zarathustra: 2001 A Space Odyssey 
		// specify the tune
		double [][] notes = { 
				     {C4,l},{0,100},{G4,l},{0,100},{C5,l},{0,100},
				     {E5,s},{0,100},{Eb5,lll},{0,500},
				     {C4,l},{0,100},{G4,l},{0,100},{C5,l},{0,100},
				     {Eb5,s},{0,100},{E5,lll},{0,500},
				     {A5,s},{0,100},{B5,s},{0,100},{C6,l},{0,100},
				     {A5,s},{0,100},{B5,s},{0,100},{C6,l},{0,100},
				     {D6,ll},{0,100},
				     {E6,s},{0,100},{F6,s},{0,100},{G6,l},{0,100},
				     {E6,s},{0,100},{F6,s},{0,100},{G6,l},{0,100},
				     {A6,l},{0,100},{B6,l},{0,100},{C7,lll},
				     };
		
		// going through the array backwards, using addNote method, using the frequencies and the note lengths in milliseconds found in the notes array 
		for(int i = notes.length-1; i >= 0; i--){
			temp = addNote(temp,notes[i][0], (int) notes[i][1]);
		}
		

		// append temp, ie the tune, to current ais 
       		return append(temp,ais);
    }

/**** altChannels *****************************************************************/

    public static AudioInputStream altChannels(AudioInputStream ais, double timeInterval){



	int frameSize 	= ais.getFormat().getFrameSize(); // = 4
    float frameRate   = ais.getFormat().getFrameRate(); 
	int frameInterval = (int) (timeInterval* frameRate);
	int inputLengthInBytes = (int) (frameInterval*frameSize);
	int numChannels     = ais.getFormat().getChannels(); // = 2
	
	// byte arrays for input channels and output channels
	byte[] ich0, ich1, och0, och1;
	byte[] a=null, b=null;

	try {



		     // create new byte arrays a for input and b for output of the right size
		     a = new byte[(int) (ais.getFrameLength()*frameSize)];
		     b = new byte[(int) (ais.getFrameLength()*frameSize)*2];
		     
		     // fill the byte array a with the data of the AudioInputStream
		     
		     ais.read(a);
		     
		     // create new byte arrays for input and output channels of the right size 	
	    	 
		     ich0 = new byte[a.length/2];
		     ich1 = new byte[a.length/2];
		     och0 = new byte[b.length/2];
		     och1 = new byte[b.length/2];
		     
		     // fill up ich0 and ich1 by splitting a
		     // fill the integer array by combining two bytes of the
		     
			    // byte array a into one integer
			    for (int i=0; i<a.length/2; i+=4) {
				ich0[i] = (byte) a[2*i];
				ich1[i] = (byte) a[2*i+2]; 
		 	    }
			    
			    int N = (int) (inputLengthInBytes/numChannels);
			    
			    int remainder = a.length % N*2;
			    
			    for(int i = 0; i < ich0.length; i+= 2*N){
			    	for(int j = 0; j < N-1; j+= 2){
			    		//make this channel silent at this value of i+j
			    		och0[i+j] = 0;
			    		// create output channels from input channels
			    		och0[(i+N)+j] = ich0[(int)(i/2)+j];
			    		och1[i+j] = ich1[(int)(i/2)+j];
			    		//make this channel silent at this value of i+j+N - N being the half the length(in bytes) of a segment
			    		och1[(i+N)+j] = 0;
			    	}
			    }
			   

		     // join och0 and och1 into b
	              for (int i=0; i < b.length; i += 4) {
	            	 //take bytes from each of the output channels and put them into the byte array b in cycles of 4
	            	  b[i]   = och0[i/2];
	            	  b[i+1] = och1[i/2];
	            	  b[i+2] = och1[i/2];
	            	  b[i+3] = och0[i/2];
	              }
	                            
	              

	} catch(Exception e){
	    System.out.println("Something went wrong");
	    e.printStackTrace();
	}
	
	// return b 
	return new AudioInputStream(new ByteArrayInputStream(b),
				    ais.getFormat(), b.length/ais.getFormat().getFrameSize());

    } // end altChannels


} // AudioManipulation
