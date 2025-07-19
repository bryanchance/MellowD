package org.mellowd.jupyter.live;

import io.github.spencerpark.jupyter.kernel.BaseKernel;
import io.github.spencerpark.jupyter.kernel.LanguageInfo;
import io.github.spencerpark.jupyter.kernel.display.DisplayData;
import io.github.spencerpark.jupyter.messages.Header;
import org.mellowd.compiler.MellowD;
import org.mellowd.io.Compiler;
import org.mellowd.io.ResourceSourceFinder;
import org.mellowd.io.SourceFinder;
import org.mellowd.io.live.MellowDSession;
import org.mellowd.jupyter.IMellowD;
import org.mellowd.midi.TimingEnvironment;
import org.mellowd.plugin.PluginLoadException;
import org.mellowd.plugin.PluginManager;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IMellowDLiveKernel extends BaseKernel {
    private static Synthesizer loadSynth(Path sfPath) throws MidiUnavailableException {
        Synthesizer synth = MidiSystem.getSynthesizer();

        System.out.println(synth.getDeviceInfo().getDescription());
        System.out.println(synth.getDeviceInfo().getName());
        System.out.println(synth.getDeviceInfo().getVendor());
        System.out.println(synth.getDeviceInfo().getVersion());

        if (!synth.isOpen()) synth.open();

        if (sfPath == null)
            return synth;

        File soundFontFile = sfPath.toAbsolutePath().toFile();

        if (!soundFontFile.isFile()) {
            System.err.printf("Sound font path '%s' cannot be resolved to a file", sfPath.toAbsolutePath().toString());
            return synth;
        }

        System.out.printf("Loading sound font '%s'...\n", sfPath);

        Soundbank soundbank;
        try {
            soundbank = MidiSystem.getSoundbank(soundFontFile);
        } catch (InvalidMidiDataException e) {
            System.err.printf("Invalid sound font '%s'. Problem: %s\n",
                    soundFontFile.getName(), e.getLocalizedMessage());
            return synth;
        } catch (IOException e) {
            System.err.printf("Error loading sound font '%s'. Problem: %s\n",
                    soundFontFile.getName(), e.getLocalizedMessage());
            return synth;
        }

        if (!synth.isSoundbankSupported(soundbank)) {
            System.err.printf("Sound font '%s' is not supported by the midi system's synthesizer.\n",
                    soundFontFile.getName());
            return synth;
        }

        boolean allLoaded = synth.loadAllInstruments(soundbank);

        System.out.printf("Loaded %s instruments from sound font %s\n",
                allLoaded ? "all" : "some", sfPath);

        return synth;
    }

    private final LanguageInfo languageInfo;
    private final String banner;
    private final List<LanguageInfo.Help> helpLinks;

    private final MellowD mellowD;
    private final MellowDSession session;
    private final PluginManager plugins;

    public IMellowDLiveKernel(Path sfPath) throws InvalidMidiDataException, MidiUnavailableException {
        this.languageInfo = new LanguageInfo.Builder("MellowD")
                .mimetype("text/x-java-source")
                .fileExtension(".mlod")
                .pygments("groovy")
                .codemirror("mellowd")
                .build();
        this.banner = String.format("MellowD %s :: IMellowD Live Kernel %s \nProtocol v%s implementation by %s %s",
                Compiler.VERSION,
                IMellowD.VERSION,
                Header.PROTOCOL_VERISON,
                KERNEL_META.getOrDefault("project", "UNKNOWN"),
                KERNEL_META.getOrDefault("version", "UNKNOWN")
        );
        this.helpLinks = new ArrayList<>(1);
        this.helpLinks.add(new LanguageInfo.Help("MellowD", "https://github.com/SpencerPark/MellowD"));

        SourceFinder srcFinder = new ResourceSourceFinder(Compiler.FILE_EXTENSION);
        TimingEnvironment timingEnv = new TimingEnvironment(4, 4, 120);

        Synthesizer synth = IMellowDLiveKernel.loadSynth(sfPath);

        this.mellowD = new MellowD(srcFinder, timingEnv);
        this.session = new MellowDSession(mellowD, synth, ".");

        this.plugins = new PluginManager();
        try {
            this.plugins.applySome(this.mellowD, Collections.singletonList("bjorklund"));
            new Random().apply(this.mellowD);
            new Pick().apply(this.mellowD);
        } catch (PluginLoadException e) {
            System.err.printf("WARNING: Could not load plugin. %s", e.getLocalizedMessage());
        }
    }

    @Override
    public String getBanner() {
        return this.banner;
    }

    @Override
    public List<LanguageInfo.Help> getHelpLinks() {
        return this.helpLinks;
    }

    @Override
    public LanguageInfo getLanguageInfo() {
        return this.languageInfo;
    }

    @Override
    public DisplayData eval(String expr) throws Exception {
        this.session.eval(expr);
        return null;
    }
}
