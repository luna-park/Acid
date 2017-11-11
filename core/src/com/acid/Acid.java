package com.acid;

import com.acid.actors.*;
import com.badlogic.gdx.*;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import synth.BasslineSynthesizer;
import synth.Output;
import synth.RhythmSynthesizer;

import java.util.ArrayList;

import static com.badlogic.gdx.input.GestureDetector.*;

public class Acid implements ApplicationListener {

    private BitmapFont font;
    private Stage stage;
    private LightActor mutateLight = null;
    private float newZoom;
    static float rainbowFade = 0f;
    private static float rainbowFadeDir = .005f;
    private Label BpmLabel;
    private com.acid.actors.SequenceActor sequenceMatrix;
    private com.acid.actors.DrumActor drumMatrix;
    private double[] knobs;
    private boolean drumsSelected;
    private float drumsSynthScale = 1.0f;
    private int prevStep = -1;

    private ArrayList<SequencerData> sequencerDataArrayList = new ArrayList<SequencerData>();
    private ArrayList<DrumData> drumDataArrayList = new ArrayList<DrumData>();
    private ArrayList<KnobData> knobsArrayList = new ArrayList<KnobData>();
    private int songPosition = 0;
    private int maxSongPosition = 0;
    private int minSongPosition = 0;
    private Label maxSongLengthLabel;
    private Label songLengthLabel;
    private Label minSongLengthLabel;
    private Label stepLabel;

    private CurrentDrumActor currentDrumActor;
    private CurrentSequencerActor currentSequencerActor;
    private CurrentKnobsActor currentKnobsActor;
    private Label minSongLengthCaption;
    private Label songLengthCaption;
    private Label maxSongLengthCaption;

    public Acid() {
    }

    @Override
    public void create() {

        Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));
        stage = new Stage();

        Statics.renderer = new ShapeRenderer();
        Statics.output = new Output();
        Statics.output.getSequencer().setBpm(120);
        Statics.output.getSequencer().randomizeRhythm();
        drumsSelected = false;
        Statics.output.getSequencer().randomizeSequence();

        InputMultiplexer mult = new InputMultiplexer();
        GestureListener gl = new GestureListener() {

            @Override
            public boolean touchDown(float x, float y, int pointer, int button) {
                return false;
            }

            @Override
            public boolean tap(float x, float y, int count, int button) {
                return false;
            }

            @Override
            public boolean longPress(float x, float y) {
                return false;
            }

            @Override
            public boolean fling(float velocityX, float velocityY, int button) {
                return false;
            }

            @Override
            public boolean pan(float x, float y, float deltaX, float deltaY) {
                ((OrthographicCamera) stage.getCamera()).translate(-deltaX / 2f, deltaY / 2f);
                return false;
            }

            @Override
            public boolean panStop(float x, float y, int pointer, int button) {
                return false;
            }

            @Override
            public boolean zoom(float initialDistance, float distance) {
                newZoom = (Math.abs(distance-initialDistance))/distance;
//				((OrthographicCamera) stage.getCamera()).zoom =initialDistance/distance;
                return true;
            }

            @Override
            public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
                return false;
            }

            @Override
            public void pinchStop() {

            }
        };
        GestureDetector gd = new GestureDetector(gl);
        mult.addProcessor(stage);
        mult.addProcessor(gd);

        InputProcessor il = new InputProcessor() {
            @Override
            public boolean keyDown(int keycode) {
                return false;
            }

            @Override
            public boolean keyUp(int keycode) {
                return false;
            }

            @Override
            public boolean keyTyped(char character) {
                return true;
            }


            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                return false;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                return false;
            }

            @Override
            public boolean scrolled(int amount) {
                return false;
            }
        };
        mult.addProcessor(il);

        Gdx.input.setInputProcessor(mult);

        font = new BitmapFont(Gdx.app.getFiles().getFileHandle("data/font.fnt",
                FileType.Internal), false);
        font.getData().setScale(.7f);
        Statics.output.start();
        Statics.synth = (BasslineSynthesizer) Statics.output.getTrack(0);
        Statics.drums = (RhythmSynthesizer) Statics.output.getTrack(1);
        Statics.output.getSequencer().drums.randomize();
        Statics.output.getSequencer().bass.randomize();
        Table table = new Table(skin);
        table.setFillParent(true);
        stage.addActor(table);

        RectangleActor rectangleActor = new RectangleActor(330, 50);
        rectangleActor.setPosition(122, 120);
        table.addActor(rectangleActor);

        currentSequencerActor = new CurrentSequencerActor(100, 100);
        currentSequencerActor.setPosition(20, 295);
        currentSequencerActor.addListener(new ActorGestureListener() {

            @Override
            public void tap(InputEvent event, float stageX, float stageY, int count, int button) {
//                SequencerData.undo();
                SequencerData.popStack();
            }

//            @Override
//            public void fling(InputEvent event, float velocityX, float velocityY, int button) {
//                System.out.println("swipe!! " + velocityX + ", " + velocityY);
//                if (velocityX > 0) SequencerData.redo();
//                if (velocityX < 0) SequencerData.popStack();
//            }
        });
        table.addActor(currentSequencerActor);


        TextButton pushtoSequencer = new TextButton(" > ", skin);
        pushtoSequencer.setPosition(100, 305);
        table.addActor(pushtoSequencer);
        pushtoSequencer.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
//                SequencerData.undo();
                if (SequencerData.peekStack() != null) SequencerData.peekStack().refresh();
                return true;
            }
        });
        TextButton popFromSequencer = new TextButton(" < ", skin);
        popFromSequencer.setPosition(100, 360);
        table.addActor(popFromSequencer);
        popFromSequencer.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                SequencerData.pushStack(new SequencerData());
                return true;
            }
        });

        currentDrumActor = new CurrentDrumActor(100, 100);
        currentDrumActor.setPosition(20, 185);
        currentDrumActor.addListener(new ActorGestureListener() {

            @Override
            public void tap(InputEvent event, float stageX, float stageY, int count, int button) {
                DrumData.popStack();
            }
//
//            @Override
//            public void fling(InputEvent event, float velocityX, float velocityY, int button) {
//                System.out.println("swipe!! " + velocityX + ", " + velocityY);
//                if (velocityX > 0) DrumData.redo();
//                if (velocityX < 0) DrumData.popStack();
//            }
        });

        table.addActor(currentDrumActor);

        TextButton pushtoDrum = new TextButton(" > ", skin);
        pushtoDrum.setPosition(100, 195);
        table.addActor(pushtoDrum);
        pushtoDrum.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
//                SequencerData.undo();
                if (DrumData.peekStack() != null) DrumData.peekStack().refresh();
                return true;
            }
        });
        TextButton popFromDrum = new TextButton(" < ", skin);
        popFromDrum.setPosition(100, 250);
        table.addActor(popFromDrum);
        popFromDrum.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                DrumData.pushStack(new DrumData());
                return true;
            }
        });


        currentKnobsActor = new CurrentKnobsActor(90, 70);
        currentKnobsActor.setPosition(455, 100);
        currentKnobsActor.addListener(new ActorGestureListener() {

//            @Override
//            public void tap(InputEvent event,float stageX, float stageY, int count, int button){
//                DrumData.undo();
//            }

            @Override
            public void fling(InputEvent event, float velocityX, float velocityY, int button) {
                System.out.println("swipe!! " + velocityX + ", " + velocityY);
//                if (velocityX > 0) KnobData.redo();
//                if (velocityX < 0) KnobData.undo();
            }
        });
        table.addActor(currentKnobsActor);
//        final Touchpad touch1 = new Touchpad(0, skin);
//        touch1.setBounds(15, 15, 100, 100);
//        touch1.addListener(new ChangeListener() {
//            @Override
//            public void changed(ChangeEvent event, Actor actor) {
//                Statics.synth.controlChange(35, (int) (touch1.getKnobX()));
//                Statics.synth.controlChange(34, (int) (touch1.getKnobY()));
//
//            }
//        });
//        touch1.setPosition(20, 190);
//        table.addActor(touch1);

//        final Touchpad touch2 = new Touchpad(0, skin);
//        touch2.setBounds(15, 15, 100, 100);
//        touch2.addListener(new ChangeListener() {
//            @Override
//            public void changed(ChangeEvent event, Actor actor) {
//                Statics.synth.controlChange(36, (int) (touch2.getKnobX()));
//                Statics.synth.controlChange(37, (int) (touch2.getKnobY()));
//
//            }
//        });
//        touch2.setPosition(20, 300);
//        table.addActor(touch2);

        table.setPosition(Gdx.graphics.getWidth() / 2 - 280,
                Gdx.graphics.getHeight() / 2 - 290);
        ((OrthographicCamera) stage.getCamera()).zoom -= .30f;
        newZoom = ((OrthographicCamera) stage.getCamera()).zoom;
        KnobActor[] mya = new KnobActor[10];
        mya[0] = new KnobActor("Tune", 0);
        table.addActor(mya[0]);
        mya[1] = new KnobActor("Cut", 1);
        table.addActor(mya[1]);
        mya[2] = new KnobActor("Res", 2);
        table.addActor(mya[2]);
        mya[3] = new KnobActor("Env", 3);
        table.addActor(mya[3]);
        mya[4] = new KnobActor("Dec", 4);
        table.addActor(mya[4]);
        mya[5] = new KnobActor("Acc", 5);
        table.addActor(mya[5]);
        mya[6] = new KnobActor("bpm", 6);
        table.addActor(mya[6]);
        mya[7] = new KnobActor("Vol", 7);
        table.addActor(mya[7]);


        //bottom row of knobs
        int hj = 130;
        int gh = 125;
        mya[0].setPosition(hj, gh);
        mya[1].setPosition(hj += 56, gh);
        mya[2].setPosition(hj += 56, gh);
        mya[3].setPosition(hj += 56, gh);
        mya[4].setPosition(hj += 56, gh);
        mya[5].setPosition(hj += 56, gh);

        mya[6].setPosition(40, 408);
        mya[7].setPosition(85, 408);

        drumMatrix = new DrumActor();
        table.addActor(drumMatrix);
        drumMatrix.setScale(drumsSynthScale);
        drumMatrix.setPosition(130, 178);

        sequenceMatrix = new SequenceActor();
        table.addActor(sequenceMatrix);
        sequenceMatrix.setScale(1f - drumsSynthScale);
        sequenceMatrix.setPosition(130, 178);


        final TextButton prevMin = new TextButton(" < ", skin);
        table.addActor(prevMin);
        prevMin.setPosition(140, 95);
        prevMin.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                if (minSongPosition > 0) {
                    minSongPosition--;
                }
                return true;
            }
        });


        minSongLengthLabel = new Label("", skin);
        minSongLengthLabel.setPosition(166, 105);
        minSongLengthLabel.setFontScale(1f);
        table.addActor(minSongLengthLabel);

        minSongLengthCaption = new Label("Start", skin);
        minSongLengthCaption.setPosition(166, 75);
        minSongLengthCaption.setFontScale(.75f);
        table.addActor(minSongLengthCaption);


        final TextButton nextMin = new TextButton(" > ", skin);
        table.addActor(nextMin);
        nextMin.setPosition(195, 95);
        nextMin.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                if (minSongPosition < maxSongPosition) minSongPosition++;

                return true;
            }
        });


        final TextButton prev = new TextButton(" < ", skin);
        table.addActor(prev);
        prev.setPosition(250, 95);
        prev.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                if (songPosition > minSongPosition) {
                    swapPattern(songPosition, --songPosition);
                }
                return true;
            }
        });

        songLengthLabel = new Label("", skin);
        songLengthLabel.setPosition(276, 105);
        songLengthLabel.setFontScale(1f);
        table.addActor(songLengthLabel);

        songLengthCaption = new Label("Current", skin);
        songLengthCaption.setPosition(266, 75);
        songLengthCaption.setFontScale(.75f);
        table.addActor(songLengthCaption);

        final TextButton next = new TextButton(" > ", skin);
        table.addActor(next);
        next.setPosition(305, 95);
        next.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                if (songPosition == maxSongPosition) return true;
                swapPattern(songPosition, ++songPosition);
                return true;
            }
        });

        final TextButton prevMax = new TextButton(" < ", skin);
        table.addActor(prevMax);
        prevMax.setPosition(363, 95);
        prevMax.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                if (maxSongPosition > 0 && maxSongPosition > minSongPosition) {
                    maxSongPosition--;
                }
                return true;
            }
        });

        maxSongLengthLabel = new Label("", skin);
        maxSongLengthLabel.setPosition(389, 105);
        maxSongLengthLabel.setFontScale(1f);
        table.addActor(maxSongLengthLabel);

        maxSongLengthCaption = new Label("End", skin);
        maxSongLengthCaption.setPosition(394, 75);
        maxSongLengthCaption.setFontScale(.75f);
        table.addActor(maxSongLengthCaption);

        final TextButton nextMax = new TextButton(" > ", skin);
        table.addActor(nextMax);
        nextMax.setPosition(418, 95);
        nextMax.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                maxSongPosition++;
                return true;
            }
        });


        final TextButton recButton = new TextButton("Rec", skin);
        recButton.setChecked(false);
        recButton.setColor(recButton.isChecked() ? Color.WHITE : Color.RED);
        table.addActor(recButton);
        recButton.setPosition(60f, 95);
        recButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y,

                                     int pointer, int button) {
                recButton.setColor(recButton.isChecked() ? Color.RED : Color.WHITE);
                Statics.recording = recButton.isChecked();
                return true;
            }
        });

        final TextButton freeButton = new TextButton("Free", skin);
        freeButton.setChecked(false);
        freeButton.setColor(freeButton.isChecked() ? Color.WHITE : Color.RED);
        table.addActor(freeButton);
        freeButton.setPosition(15f, 95);
        freeButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y,

                                     int pointer, int button) {
                freeButton.setColor(freeButton.isChecked() ? Color.RED : Color.WHITE);
                Statics.free = freeButton.isChecked();
                return true;
            }
        });

        final TextButton prevStep = new TextButton(" < ", skin);
        table.addActor(prevStep);
        prevStep.setPosition(35f, 135);
        prevStep.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y,

                                     int pointer, int button) {
                Statics.output.getSequencer().step = (16 + Statics.output.getSequencer().step - 1) % 16;
                return true;
            }
        });
        stepLabel = new Label("", skin);
        stepLabel.setPosition(60, 148);
        stepLabel.setFontScale(1.5f);
        table.addActor(stepLabel);


        final TextButton nextStep = new TextButton(" > ", skin);
        table.addActor(nextStep);
        nextStep.setPosition(90f, 135);
        nextStep.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y,

                                     int pointer, int button) {
                Statics.output.getSequencer().step = (Statics.output.getSequencer().step + 1) % 16;
                return true;
            }
        });


        final TextButton pauseButton = new TextButton(" || ", skin);
        pauseButton.setChecked(true);
        table.addActor(pauseButton);
        pauseButton.setPosition(100f, 95);
        pauseButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y,

                                     int pointer, int button) {
                Statics.output.paused = pauseButton.isChecked();
                pauseButton.setColor(pauseButton.isChecked() ? Color.RED : Color.WHITE);
                return true;
            }
        });

        final TextButton waveButton = new TextButton(" ~ ", skin);
        table.addActor(waveButton);
        waveButton.setPosition(520f, 180);
        waveButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                Statics.output.getSequencer().bass.switchWaveform();
                waveButton.setChecked(waveButton.isChecked());
                waveButton.setColor(waveButton.isChecked() ? Color.WHITE : Color.RED);
                waveButton.setText(waveButton.isChecked() ? " ~ " : " ^ ");
                waveButton.invalidate();
                return true;
            }
        });

        TextButton mutateButton = new TextButton("Mutate", skin);
        table.addActor(mutateButton);
        mutateButton.setPosition(470, 260);
        mutateButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                if (drumsSelected) {
                    Statics.mutateDrum = !Statics.mutateDrum;
                    mutateLight.on = Statics.mutateDrum;
                } else {
                    Statics.mutateSynth = !Statics.mutateSynth;
                    mutateLight.on = Statics.mutateSynth;
                }

                return true;
            }
        });

        TextButton randomButton = new TextButton("Random", skin);
        table.addActor(randomButton);
        randomButton.setPosition(470, 300);
        randomButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {

                if (drumsSelected) {
                    Statics.output.getSequencer().randomizeRhythm();
                    new DrumData();
                } else {
                    Statics.output.getSequencer().randomizeSequence();
                    //new SequencerData();
                }
                return true;
            }
        });

        TextButton synthButton = new TextButton("Synth", skin);
        table.addActor(synthButton);
        synthButton.setPosition(470, 180);
        synthButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                drumsSelected = false;
                mutateLight.on = Statics.mutateSynth;
                return true;
            }
        });

        TextButton clearButton = new TextButton("Clear", skin);
        table.addActor(clearButton);
        clearButton.setPosition(470, 340);
        clearButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                if (drumsSelected) {
                    for (int i = 0; i < Statics.output.getSequencer().rhythm.length; i++) {
                        for (int j = 0; j < Statics.output.getSequencer().rhythm[0].length; j++) {
                            Statics.output.getSequencer().rhythm[i][j] = 0;
                        }
                    }
                } else {
                    for (int i = 0; i < 16; i++) {
                        Statics.output.getSequencer().bassline.pause[i] = true;
                    }
                }

                return true;
            }
        });

        TextButton drumsButton = new TextButton("Drums", skin);
        table.addActor(drumsButton);
        drumsButton.setPosition(470, 220);
        drumsButton.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                drumsSelected = true;
                mutateLight.on = Statics.mutateDrum;
                return true;
            }
        });

        mutateLight = new LightActor(5, null, false);
        table.addActor(mutateLight);
        mutateLight.setPosition(455, 268);
        mutateLight.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                mutateLight.on = !mutateLight.on;
                if (drumsSelected) {
                    Statics.mutateDrum = mutateLight.on;
                } else {
                    Statics.mutateSynth = mutateLight.on;
                }
                return true;
            }
        });


        BpmLabel = new Label("", skin);
        BpmLabel.setPosition(52, 407);
        BpmLabel.setFontScale(.5f);
        table.addActor(BpmLabel);


        if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            TextButton zi = new TextButton("Zoom +", skin);
            table.addActor(zi);
            zi.setPosition(470, 430);
            zi.addListener(new InputListener() {
                public boolean touchDown(InputEvent event, float x, float y,
                                         int pointer, int button) {
                    newZoom -= .05f;
                    return true;
                }
            });


            TextButton zo = new TextButton("Zoom - ", skin);
            table.addActor(zo);
            zo.setPosition(470, 400);
            zo.addListener(new InputListener() {
                public boolean touchDown(InputEvent event, float x, float y,
                                         int pointer, int button) {
                    newZoom += .05f;
                    return true;
                }
            });
        }
        ;

        final LightActor synthLight = new LightActor(5, null, true);
        table.addActor(synthLight);
        synthLight.setPosition(455, 188);
        synthLight.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                synthLight.on = !synthLight.on;
                Statics.synthOn = synthLight.on;
                return true;
            }
        });

        final LightActor drumsLight = new LightActor(5, null, true);
        table.addActor(drumsLight);
        drumsLight.setPosition(455, 228);
        drumsLight.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                drumsLight.on = !drumsLight.on;
                Statics.drumsOn = drumsLight.on;
                return true;
            }
        });


        SequencerData.pushStack(new SequencerData());
        DrumData.pushStack(new DrumData());
        new KnobData();
        newZoom += .10f;
    }

    private void swapPattern(int curr, int next) {
        while (next >= sequencerDataArrayList.size()) {
            sequencerDataArrayList.add(new SequencerData());
            drumDataArrayList.add(new DrumData());
            knobsArrayList.add(KnobData.currentSequence);
        }
        if (Statics.recording) {
            sequencerDataArrayList.remove(curr);
            drumDataArrayList.remove(curr);
            knobsArrayList.remove(curr);
            sequencerDataArrayList.add(curr, new SequencerData());
            drumDataArrayList.add(curr, new DrumData());
            knobsArrayList.add(curr, KnobData.factory());
        }
        if (!Statics.free) {
            sequencerDataArrayList.get(next).refresh();
            drumDataArrayList.get(next).refresh();
            if (!KnobImpl.isTouched()) KnobData.setcurrentSequence(knobsArrayList.get(next));

        }
    }

    @Override
    public void resume() {
//		Output.running=true;
        Output.resume();

    }

    @Override
    public void render() {


        if (Statics.mutateDrum & Math.random() < .01d) {
            drumMatrix.ttouch((int) (MathUtils.random() * 16),
                    (int) (MathUtils.random() * 31) - 16);
            new DrumData();
        }
        if (Statics.mutateSynth & Math.random() < .01d) {
            sequenceMatrix.ttouch((int) (MathUtils.random() * 16),
                    (int) (MathUtils.random() * 31) - 16);
            //new SequencerData();
        }

        // mya.rotate(10);
//        Color c=ColorHelper.rainbowDark();
        Color c = Color.BLACK;
        Gdx.gl.glClearColor(c.r, c.g, c.b, c.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Gdx.graphics.getDeltaTime());
        if (newZoom < ((OrthographicCamera) stage.getCamera()).zoom)
            ((OrthographicCamera) stage.getCamera()).zoom -= .02f;
        if (newZoom > ((OrthographicCamera) stage.getCamera()).zoom)
            ((OrthographicCamera) stage.getCamera()).zoom += .02f;
        stage.draw();
//        stage.getBatch().begin();
//        font.setColor(ColorHelper.rainbow());
//        font.draw(stage.getBatch(),
//                (int) Statics.output.getSequencer().bpm + "", 90, 360);
//        stage.getBatch().end();

        if (KnobImpl.getControl(Statics.output.getSequencer().step) != null)
            for (int i = 0; i < 8; i++) {
                if (!KnobImpl.touched[i]) {
                    if (Statics.free) {

                    } else {
                        KnobImpl.setControls(KnobImpl.getControl(Statics.output.getSequencer().step)[i], i);
                    }
                } else {
                    new KnobData();
                    if (Statics.recording) {
                        KnobImpl.setControl(Statics.output.getSequencer().step, i);
                    }
                }

            }
//            KnobImpl.setControls(KnobImpl.getControl(Statics.output.getSequencer().step));

        if (Statics.output.getSequencer().step % 16 == 0 && prevStep % 16 == 15) {
            int old = songPosition;
            if (!Statics.free) songPosition++;
            if (songPosition > maxSongPosition) {
                songPosition = minSongPosition;
            }
            swapPattern(old, songPosition);
        }
        prevStep = Statics.output.getSequencer().step;

        BpmLabel.setColor(ColorHelper.rainbowLight());
        BpmLabel.setText((int) Statics.output.getSequencer().bpm + "");

        minSongLengthCaption.setColor(ColorHelper.rainbowLight());
        songLengthCaption.setColor(ColorHelper.rainbowLight());
        maxSongLengthCaption.setColor(ColorHelper.rainbowLight());

        maxSongLengthLabel.setColor(ColorHelper.rainbowLight());
        maxSongLengthLabel.setText(format(maxSongPosition + 1));
        minSongLengthLabel.setColor(ColorHelper.rainbowLight());
        minSongLengthLabel.setText((format(minSongPosition + 1)));
        songLengthLabel.setColor(ColorHelper.rainbowLight());
        songLengthLabel.setText(format(songPosition + 1));
        stepLabel.setColor(ColorHelper.rainbowLight());
        int step = Statics.output.getSequencer().step % 16 + 1;
        stepLabel.setText(step < 10 ? "0" + step : "" + step);

        rainbowFade += rainbowFadeDir;
        while (rainbowFade < 0f || rainbowFade > 1f) {
            rainbowFadeDir = -rainbowFadeDir;
            rainbowFade += rainbowFadeDir;
//            rainbowFadeDir+= (Math.random()-.5f)/10f;
        }
        if (drumsSelected && drumsSynthScale < 1f) {
            drumsSynthScale += .05f;
            drumMatrix.setScale(drumsSynthScale);
            sequenceMatrix.setScale(1.0f - drumsSynthScale);
        }
        if (!drumsSelected && drumsSynthScale > 0f) {
            drumsSynthScale -= .05f;
            drumMatrix.setScale(drumsSynthScale);
            sequenceMatrix.setScale(1.0f - drumsSynthScale);
        }


    }

    private String format(int i) {
        String s = i + "";
        if (i < 100) s = "0" + s;
        if (i < 10) s = "00" + i;
        return s;
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().setScreenSize(width, height);
    }

    @Override
    public void pause() {
        Output.pause();
    }

    @Override
    public void dispose() {
        Output.running = false;
        Statics.output.dispose();
    }
}
