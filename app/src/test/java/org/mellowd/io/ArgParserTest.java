package org.mellowd.io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ArgParserTest {

    private static void testThrowsException(String failMessage, String... args) {
        try {
            ArgParser.parse(args);
        } catch (ArgParser.Help e) {
            return;
        }

        fail(failMessage);
    }

    @Test
    public void timeSigFlag() throws Exception {
        String[] args = new String[]{
                "--timesig", "3", "8"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertEquals(3, options.getTimeSignatureTop(), "Incorrect time signature numerator set in the options");
        assertEquals(8, options.getTimeSignatureBottom(), "Incorrect time signature denominator set in the options");
    }

    @Test
    public void timeSigShortFlag() throws Exception {
        String[] args = new String[]{
                "-ts", "14", "16"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertEquals(14, options.getTimeSignatureTop(), "Incorrect time signature numerator set in the options");
        assertEquals(16, options.getTimeSignatureBottom(), "Incorrect time signature denominator set in the options");
    }

    @Test
    public void timeSigFlagValuesMissing() throws Exception {
        testThrowsException("No exception thrown when time sig flag given but values missing",
                "--timesig"
        );
        testThrowsException("No exception thrown when time sig flag given but denominator missing",
                "--timesig", "3"
        );
    }

    @Test
    public void timeSigFlagValueNotInteger() throws Exception {
        testThrowsException("No exception thrown when time sig denominator is NaN",
                "--timesig", "4", "a"
        );
        testThrowsException("No exception thrown when time sig numerator is a decimal",
                "--timesig", "4.4", "5"
        );
    }

    @Test
    public void tempoFlag() throws Exception {
        String[] args = new String[]{
                "--tempo", "130"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertEquals(130, options.getTempo(), "Incorrect tempo set in the options");
    }

    @Test
    public void tempoShortFlag() throws Exception {
        String[] args = new String[]{
                "-t", "160"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertEquals(160, options.getTempo(), "Incorrect tempo set in options");
    }

    @Test
    public void tempoFlagValueMissing() throws Exception {
        testThrowsException("No exception thrown when tempo flag given but no tempo given",
                "--tempo"
        );
    }


    @Test
    public void tempoFlagNegative() throws Exception {
        testThrowsException("No exception thrown when tempo flag is negative",
                "--tempo", "-4"
        );
    }

    @Test
    public void tempoFlagDecimal() throws Exception {
        testThrowsException("No exception thrown when tempo flag is a decimal",
                "--tempo", "4.1"
        );
    }

    @Test
    public void outDirFlag() throws Exception {
        String[] args = new String[]{
                "--outdir", "build"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertEquals("build", options.getOutputDirectory(), "Incorrect output directory set in the options");
    }

    @Test
    public void outDirShortFlag() throws Exception {
        String[] args = new String[]{
                "-o", "build"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertEquals("build", options.getOutputDirectory(), "Incorrect output directory set in the options");
    }

    @Test
    public void outDirFlagValueMissing() throws Exception {
        testThrowsException("No exception thrown when output directory flag given but no output directory",
                "--outdir"
        );
    }

    @Test
    public void sourceFlag() throws Exception {
        String[] args = new String[]{
                "--src", "src1"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertEquals("src1", options.getSourceDirs().get(0), "Source directory not added to the options");
    }

    @Test
    public void sourceShortFlag() throws Exception {
        String[] args = new String[]{
                "-s", "src1"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertEquals("src1", options.getSourceDirs().get(0), "Source directory not added to the options");
    }

    @Test
    public void sourceFlagMultiple() throws Exception {
        String[] args = new String[]{
                "--src", "src1", "--src", "src2"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertEquals("src1", options.getSourceDirs().get(0), "Source directory not properly added to the options");
        assertEquals("src2", options.getSourceDirs().get(1), "Second source directory not properly added to ");
    }

    @Test
    public void sourceFlagValueMissing() throws Exception {
        testThrowsException("No exception thrown when source flag given but value is missing",
                "--src"
        );
    }

    @Test
    public void soundFontFlag() throws Exception {
        String[] args = new String[]{
                "--soundfont", "font.sf2"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertEquals("font.sf2", options.getSoundFonts().get(0), "Sound font not added to the options");
    }

    @Test
    public void soundFontShortFlag() throws Exception {
        String[] args = new String[]{
                "-sf", "font.sf2"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertEquals("font.sf2", options.getSoundFonts().get(0), "Sound font not added to the options");
    }

    @Test
    public void soundFontFlagMultiple() throws Exception {
        String[] args = new String[]{
                "--soundfont", "font.sf2", "--soundfont", "font1.sf2"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertEquals("font.sf2", options.getSoundFonts().get(0), "Sound font not properly added to the options");
        assertEquals("font1.sf2", options.getSoundFonts().get(1), "Second sound font not properly added to ");
    }

    @Test
    public void soundFontFlagValueMissing() throws Exception {
        testThrowsException("No exception thrown when sound font flag given but value is missing",
                "--soundfont"
        );
    }

    @Test
    public void playFlag() throws Exception {
        String[] args = new String[]{
                "--play"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertTrue(options.shouldPlayLive());
        assertFalse(options.shouldOutputMIDI(), "MIDI output flag is set when --play is given");
        assertFalse(options.shouldOutputWAV(), "WAVE output flag is set when --play is given");
    }

    @Test
    public void playShortFlag() throws Exception {
        String[] args = new String[]{
                "-p"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertTrue(options.shouldPlayLive(), "Play flag not set when -p is given");
        assertFalse(options.shouldOutputMIDI(), "MIDI output flag is set when -p is given");
        assertFalse(options.shouldOutputWAV(), "WAVE output flag is set when -p is given");
    }

    @Test
    public void outputMIDIFlag() throws Exception {
        String[] args = new String[]{
                "--midi"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertTrue(options.shouldOutputMIDI(), "MIDI output flag not set when --midi is given");
        assertFalse(options.shouldPlayLive(), "Play flag is set when --midi is given");
        assertFalse(options.shouldOutputWAV(), "WAVE output flag is set when --midi is given");
    }

    @Test
    public void outputMIDIShortFlag() throws Exception {
        String[] args = new String[]{
                "-mid"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertTrue(options.shouldOutputMIDI(), "MIDI output flag not set when -mid is given");
        assertFalse(options.shouldPlayLive(), "Play flag is set when -mid is given");
        assertFalse(options.shouldOutputWAV(), "WAVE output flag is set when -mid is given");
    }

    @Test
    public void outputWAVEFlag() throws Exception {
        String[] args = new String[]{
                "--wave"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertTrue(options.shouldOutputWAV(), "WAVE output flag not set when --wave is given");
        assertFalse(options.shouldPlayLive(), "Play flag is set when --wave is given");
        assertFalse(options.shouldOutputMIDI(), "MIDI output flag is set when --wave is given");
    }

    @Test
    public void outputWAVEShortFlag() throws Exception {
        String[] args = new String[]{
                "-wav"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertTrue(options.shouldOutputWAV(), "WAVE output flag not set when -wav is given");
        assertFalse(options.shouldPlayLive(), "Play flag is set when -wav is given");
        assertFalse(options.shouldOutputMIDI(), "MIDI output flag is set when -wav is given");
    }

    @Test
    public void multiOutputFlags() throws Exception {
        String[] args = new String[]{
                "-wav", "-p", "--midi"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertTrue(options.shouldOutputWAV(), "WAVE output flag not set when all output flags were given");
        assertTrue(options.shouldOutputMIDI(), "MIDI output flag not set when all output flags were given");
        assertTrue(options.shouldPlayLive(), "Play live output flag not set when all output flags were given");
    }

    @Test
    public void silentFlag() throws Exception {
        String[] args = new String[]{
                "--silent"
        };

        CompilerOptions options = ArgParser.parse(args);

        assertTrue(options.wantsSilent(), "Silent flag not set when --silent is given");
    }

    @Test
    public void defaults() throws Exception {
        String[] args = new String[]{};

        CompilerOptions options = ArgParser.parse(args);

        assertEquals(4, options.getTimeSignatureTop(), "Time signature is not 4/4 by default");
        assertEquals(4, options.getTimeSignatureBottom(), "Time signature is not 4/4 by default");

        assertEquals(120, options.getTempo(), "Tempo is not 120 by default");

        assertEquals("", options.getOutputDirectory(), "Output directory is not the working directory by default");

        assertTrue(options.getSourceDirs().isEmpty(), "Source directories are not empty by default");

        assertTrue(options.getSoundFonts().isEmpty(), "Sound fonts are not empty by default");

        assertFalse(options.shouldPlayLive(), "Play flag not disabled by default");
        assertTrue(options.shouldOutputMIDI(), "MIDI output flag not enabled by default");
        assertFalse(options.shouldOutputWAV(), "WAVE output flag not disabled by default");

        assertFalse(options.wantsSilent(), "Silent not disabled by default");
    }
}