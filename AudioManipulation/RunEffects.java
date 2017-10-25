import javax.sound.sampled.AudioInputStream;

import fx.Effect;

public class RunEffects
{
	public static void main(String[] args)
	{
		// use these lines to register new effects
		fx.Effects.registerEffect(new Echo());
		fx.Effects.registerEffect(new ScaleToZero());
		fx.Effects.registerEffect(new AddNote());
		fx.Effects.registerEffect(new Tune());
		fx.Effects.registerEffect(new AlternateChannels());

		main.Main.init();
	}
}

// extend Effect for each 
// effect you want to do
class Echo extends Effect
{
	public Echo()
	{
		this.name = "echo";
	}
	
	public AudioInputStream process(AudioInputStream ais)
	{
		// runs the students' code
		return AudioManipulation.echo(ais, 500, 0.65, 0.35);
	}		
}

// extend Effect for each 
// effect you want to do
class AddNote extends Effect
{
	public AddNote()
	{
		this.name = "addNote";
	}
	
    public AudioInputStream process(AudioInputStream ais)
	{
		// runs the students' code
            	return AudioManipulation.addNote(ais, 400, 5000);
	}
}

// extend Effect for each 
// effect you want to do
class Tune extends Effect
{
	public Tune()
	{
		// change the name field to
		// something meaningful
		this.name = "tune";
	}
	
    public AudioInputStream process(AudioInputStream ais)
	{
		// runs the students' code
            	return AudioManipulation.tune(ais);
	}
}

// extend Effect for each 
// effect you want to do
class ScaleToZero extends Effect
{
	public ScaleToZero()
	{
		// change the name field to
		// something meaningful
		this.name = "scaleToZero";
	}
	
    public AudioInputStream process(AudioInputStream ais)
	{
		// runs the students' code
            	return AudioManipulation.scaleToZero(ais);
	}
}

// extend Effect for each 
// effect you want to do
class AlternateChannels extends Effect
{
	public AlternateChannels()
	{
		// change the name field to
		// something meaningful
		this.name = "altChannels";
	}
	
	public AudioInputStream process(AudioInputStream ais)
	{
		// runs the students' code
		return AudioManipulation.altChannels(ais, 2.0);
	}		
}
