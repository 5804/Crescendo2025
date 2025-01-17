package frc.robot;

import java.util.List;

// import com.ctre.phoenix.music.Orchestra;
// import com.ctre.phoenix6.mechanisms.swerve.SwerveModuleConstants;
import com.ctre.phoenix6.signals.NeutralModeValue;
import com.pathplanner.lib.auto.NamedCommands;
import com.pathplanner.lib.commands.PathPlannerAuto;
// import com.pathplanner.lib.util.HolonomicPathFollowerConfig;
// import com.pathplanner.lib.util.PIDConstants;
// import com.pathplanner.lib.util.ReplanningConfig;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.HolonomicDriveController;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.controller.RamseteController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryConfig;
import edu.wpi.first.math.trajectory.TrajectoryGenerator;
import edu.wpi.first.math.trajectory.constraint.DifferentialDriveVoltageConstraint;
import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.RamseteCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.SwerveControllerCommand;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.button.POVButton;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine.Direction;
import frc.robot.Constants.AutoConstants;
import frc.robot.commands.TeleopSwerve;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.LED;
import frc.robot.subsystems.Shooter;
import frc.robot.subsystems.Swerve;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {

    /* Controllers */
    // private final CommandXboxController controller = new CommandXboxController(0);
    private final CommandXboxController driver = new CommandXboxController(0);
    private final Joystick buttonBoard = new Joystick(1);
    Timer timer = new Timer();
    /* Drive Controls */
    private final int translationAxis = XboxController.Axis.kLeftY.value;
    private final int strafeAxis = XboxController.Axis.kLeftX.value;
    private final int rotationAxis = XboxController.Axis.kRightX.value;

    final JoystickButton b1 = new JoystickButton(buttonBoard, 1);
    final JoystickButton b2 = new JoystickButton(buttonBoard, 2);
    final JoystickButton b3 = new JoystickButton(buttonBoard, 3);
    final JoystickButton b4 = new JoystickButton(buttonBoard, 4);
    final JoystickButton b5 = new JoystickButton(buttonBoard, 5);
    final JoystickButton b6 = new JoystickButton(buttonBoard, 6);
    final JoystickButton b7 = new JoystickButton(buttonBoard, 7);
    final JoystickButton b8 = new JoystickButton(buttonBoard, 8);
    final JoystickButton b9 = new JoystickButton(buttonBoard, 9);
    final JoystickButton b10 = new JoystickButton(buttonBoard, 10);
    final JoystickButton b11 = new JoystickButton(buttonBoard, 11);
    final JoystickButton b12 = new JoystickButton(buttonBoard, 12);

    Trigger bbStickF = new Trigger(() -> buttonBoard.getRawAxis(1) > 0.7);
    Trigger bbStickB = new Trigger(() -> buttonBoard.getRawAxis(1) < -0.7);

    private ShuffleboardTab tab = Shuffleboard.getTab("Tab1");

    /* Subsystems */
    public final Swerve s_Swerve = new Swerve();
    public static final Shooter shooterSubsystem = new Shooter();
    public static final Intake intakeSubsystem = new Intake();
    public final LED LEDSubsystem = new LED();

    private final SendableChooser<Command> chooser = new SendableChooser<>();

    // Create config for trajectory
        TrajectoryConfig config =
            new TrajectoryConfig(
                    4, // 3
                    2) // 2
                // Add kinematics to ensure max speed is actually obeyed
                .setKinematics(Constants.Swerve.swerveKinematics);
                // Apply the voltage constraint
                // .addConstraint(autoVoltageConstraint);

        // An example trajectory to follow. All units in meters.
        Trajectory testTrajectory =
            TrajectoryGenerator.generateTrajectory(
                // Start at the origin facing the +X direction
                new Pose2d(0, 0, new Rotation2d(0)),
                // Pass through these two interior waypoints, making an 's' curve path
                List.of(new Translation2d(0.1, 0)),
                // End 3 meters straight ahead of where we started, facing forward
                new Pose2d(0, 0, new Rotation2d(90)),
                // Pass config
                config);

    /** The container for the robot. Contains subsystems, OI devices, and commands. */
    public RobotContainer() {
        NamedCommands.registerCommand("transform", transform());
        NamedCommands.registerCommand("intake", autoIntake());
        NamedCommands.registerCommand("shoot", smartShoot());
        NamedCommands.registerCommand("startShoot", startShoot());
        NamedCommands.registerCommand("thirdNoteShoot", smartShootThirdNote());
        NamedCommands.registerCommand("lastNoteShoot", smartShootLastNote());
        NamedCommands.registerCommand("positionShooter", shooterSubsystem.shootFromNotePosition());
        // NamedCommands.registerCommand("shootNote", autoShooter());
        // NamedCommands.registerCommand("indexNote", autoIndexer());
        // NamedCommands.registerCommand("stopShooter", stopShooter());
        // NamedCommands.registerCommand("stopIndexer", stopIndexer());
        NamedCommands.registerCommand("shootWithTOF", shootWithTOF());

        NamedCommands.registerCommand("index", autoIndex());
        NamedCommands.registerCommand("stow", stowParallel());
        NamedCommands.registerCommand("secondNoteShoot", smartSecondNoteShoot());
        NamedCommands.registerCommand("limelightAim", limeLightAutoAim());
        NamedCommands.registerCommand("aimAndShoot", aimAndShoot());
        NamedCommands.registerCommand("aimAndShootAuto", aimAndShootAuto());

        NamedCommands.registerCommand("shooot", smartShaneNoteShoot());
        NamedCommands.registerCommand("defaultShooterAngle", defaultShooterAngle());
        

        

        s_Swerve.resetModulesToAbsolute();
        s_Swerve.resetModulesToAbsolute();

        s_Swerve.setDefaultCommand(
            new TeleopSwerve(
                s_Swerve, 
                () -> -driver.getRawAxis(translationAxis), 
                () -> -driver.getRawAxis(strafeAxis), 
                () -> -driver.getRawAxis(rotationAxis)
                //() -> {return false;}
            )
        );

        // shooterSubsystem.setDefaultCommand(
        //     indexWithTOF()
        // );
        
        // Configure the button bindings
        configureButtonBindings();

        chooser.setDefaultOption("One Note Auto", oneNoteAuto());

        /*chooser.addOption("NEW 4 Piece Auto", newFourPieceAuto()
        .finallyDo(() -> {s_Swerve.zeroHeading();})
        ); */

      /*   chooser.addOption("4 Piece Speaker Auto", altFourPieceAuto()
        .finallyDo(() -> {s_Swerve.zeroHeading();})
        ); */

        // chooser.addOption("MidSpeaker5NoteAuto", MidSpeaker5NoteAuto()
        // .finallyDo(() -> {s_Swerve.zeroHeading();})
        // );

        // chooser.addOption("MidSpeaker5NoteAutoBlue", MidSpeaker5NoteAutoBlue()
        // .finallyDo(() -> {s_Swerve.zeroHeading();})
        // );

        // chooser.addOption("MidSpeaker4NoteAuto", MidSpeaker4NoteAuto()
        // .finallyDo(() -> {s_Swerve.zeroHeading();})
        // );

        //  chooser.addOption("crazyAuto", crazyAuto()
        // .finallyDo(() -> {s_Swerve.zeroHeading();})
        // ); 

        chooser.addOption("1m Straight",
             new PathPlannerAuto("1mStraightPathAuto"));

        // chooser.addOption("2 Piece Auto Test", twoNoteAuto()
        // .finallyDo(() -> {s_Swerve.zeroHeading();})
        // );

        //  chooser.addOption("fivebutbetterauto", fivebutbetterauto()
        //      .finallyDo(() -> {s_Swerve.zeroHeading();})
        //      );

        // chooser.addOption("4 Piece Auto", fourNoteAuto()
        // .finallyDo(() -> {s_Swerve.zeroHeading();})
        // );
        // chooser.addOption("Midfield Auto", rightMidfieldAuto()
        // .finallyDo(() -> {s_Swerve.zeroHeading();})
        // );

        // chooser.addOption("Mid Auto", midAuto());
        // chooser.addOption("Outside Auto", outsideAuto());

        // chooser.addOption("Mid Test", midTestAuto());
        // chooser.addOption("ampSideAuto", ampSideAuto());

        // chooser.addOption("Four Note Test", fourNoteTest());

        // chooser.addOption("PID Test", pidTest());

        // chooser.addOption("Choreo Test", ChoreoTest()
        // .finallyDo(() -> {s_Swerve.zeroHeading();})
        // );

        chooser.addOption("5 Note Auto", Mid5NoteChoreo()
        .finallyDo(() -> {s_Swerve.zeroHeading();})
        );
        
        chooser.addOption("4 Note Auto", Mid4NoteFinal()
        .finallyDo(() -> {s_Swerve.zeroHeading();})
        );

        chooser.addOption("Outside Auto", Outside3NoteChoreo()
        .finallyDo(() -> {s_Swerve.zeroHeading();})
        );

        chooser.addOption("Better Outside Auto", BetterOutside()
        .finallyDo(() -> {s_Swerve.zeroHeading();})
        );

       /*  chooser.addOption("Quick Test", quickTest()
        .finallyDo(() -> {s_Swerve.zeroHeading();})
        ); */
        
        // chooser.addOption("DO NOT USE YET", midfieldAuto()
        // .finallyDo(() -> {s_Swerve.zeroHeading();})
        // // );
        // chooser.addOption("Midfield Auto", midfield()
        // .finallyDo(() -> {s_Swerve.zeroHeading();})
        // );
        // chooser.addOption("Midfield Auto Test", newMidfieldAuto()
        // .finallyDo(() -> {s_Swerve.zeroHeading();})
        // );

        SmartDashboard.putData("Auto choices", chooser);
        tab.add("Auto Chooser", chooser);
    }

    /**
     * Use this method to define your button->command mappings. Buttons can be created by
     * instantiating a {@link GenericHID} or one of its subclasses ({@link
     * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing it to a {@link
     * edu.wpi.first.wpilibj2.command.button.JoystickButton}.
     */
    private void configureButtonBindings() {
        /* Driver Buttons */
        driver.x().onTrue(transform());

        // Stow Shooter and Intake
        driver.a().onTrue(stowParallel());
        
        driver.b().onTrue(shooterSubsystem.amp());

        // driver.y().whileTrue(limelightAutoAimAlign());
        //driver.y().whileTrue(limelightAimAndFire());

        driver.y().whileTrue(new ParallelCommandGroup(limelightAimAndFire(), LEDSubsystem.Red()));

        // driver.y().onFalse(new InstantCommand(() -> shooterSubsystem.setAnglePosition(0)));
        

        
        // driver.x().whileTrue(s_Swerve.sysIdQuasistatic(Direction.kForward));
        // driver.y().whileTrue(s_Swerve.sysIdQuasistatic(Direction.kReverse));
        // driver.b().whileTrue(s_Swerve.sysIdDynamic(Direction.kReverse));
        // driver.a().whileTrue(s_Swerve.sysIdDynamic(Direction.kForward));

        driver.start().onTrue(new InstantCommand(() -> s_Swerve.zeroHeading()));

        
        driver.rightTrigger(0.5).whileTrue((
            new InstantCommand(() -> {
                if (shooterSubsystem.angleEncoder.getAbsolutePosition().getValueAsDouble() < 0.135) {
                    shooterSubsystem.setAnglePosition(0.015);//.015
                }
            })
            .andThen(shooterSubsystem.setShooterSpeed(1))
            // .until(() -> {return shooterSubsystem.leftShooterMotor.getVelocity().getValue() > 95;})
            .andThen(new ParallelCommandGroup(shooterSubsystem.setIndexerSpeedNoFinallyDo(.8), LEDSubsystem.Red()))
            .finallyDo(() -> {
                shooterSubsystem.load(0);
                shooterSubsystem.shoot(0);
                if (shooterSubsystem.angleEncoder.getAbsolutePosition().getValueAsDouble() < 0.135) {
                    shooterSubsystem.setAnglePosition(0);
                }
            
            })));
            
        driver.leftTrigger(.2).whileTrue(smartIntake());

        driver.leftBumper().onTrue(new InstantCommand(() -> {shooterSubsystem.deactivateRatchet();}));

        // CLIMB - DISABLE WHEN IT'S NOT A COMPETITION
        driver.rightBumper().whileTrue(smartClimb());
        driver.rightBumper().onTrue(LEDSubsystem.Red());

        driver.back().whileTrue(limelightAutoAlignAndFireAtAmp()); 

        driver.povUp().whileTrue(shooterSubsystem.increaseShooterPos());
        driver.povDown().whileTrue(shooterSubsystem.decreaseShooterPos());

        b1.onTrue(shooterSubsystem.climbPosition());
        b5.whileTrue(smartOuttake());
        b6.whileTrue(runIntake());
        b3.onTrue(new InstantCommand(() -> {shooterSubsystem.deactivateRatchet();}));
        b2.onTrue(new InstantCommand(() -> {shooterSubsystem.activateRatchet();}));

        // driver.povRight().whileTrue(intakeSubsystem.raiseIntake());
        // driver.povLeft().whileTrue(intakeSubsystem.lowerIntake());

    }

    public Command transform() {
        return shooterSubsystem.deploy()
            .until(() -> {return shooterSubsystem.angleEncoder.getAbsolutePosition().getValueAsDouble() > 0.125;}) //0.13
            .andThen(intakeSubsystem.deploy())
            .until(() -> {return intakeSubsystem.angleEncoder.getAbsolutePosition().getValueAsDouble() > 0.33;})
            .andThen(shooterSubsystem.stow())
            .withName("OUT (TRANSFORM)");
            // .until(() -> {return shooterSubsystem.angleEncoder.getAbsolutePosition().getValue() < 0.05;});
    }

    // Deprecated
    public Command stow() {
        return shooterSubsystem.deploy()
            .until(() -> {return shooterSubsystem.angleEncoder.getAbsolutePosition().getValueAsDouble() > 0.13;}) 
            .andThen(intakeSubsystem.stow())
            .until(() -> {return intakeSubsystem.angleEncoder.getAbsolutePosition().getValueAsDouble() < 0.05;})
            .andThen(shooterSubsystem.stow());
            // .until(() -> {return shooterSubsystem.angleEncoder.getAbsolutePosition().getValue() < 0.05;})
    }

    public Command stowParallel() {
        return new ParallelCommandGroup(shooterSubsystem.deploy(), intakeSubsystem.stow())
            .until(() -> {return intakeSubsystem.angleEncoder.getAbsolutePosition().getValueAsDouble() < 0.05;}) // 0.05 // WIP
            .andThen(shooterSubsystem.stow());
            // .until(() -> {return shooterSubsystem.angleEncoder.getAbsolutePosition().getValue() < 0.05;})
    }

    public Command indexWithTOF() {
        return shooterSubsystem.setIndexerSpeed(-0.3) // -0.05
            .until(() -> {return shooterSubsystem.TOF.getRange() > 165;})
            .andThen(new ParallelCommandGroup(shooterSubsystem.setIndexerSpeedRunOnce(0), LEDSubsystem.Green()))
            .withName("INDEX NOTE WITH TOF");
    }

    public Command autoIndex() {
        return shooterSubsystem.setIndexerSpeed(-0.05) // -0.05
            .until(() -> {return shooterSubsystem.TOF.getRange() > 165;})
            .andThen(new ParallelCommandGroup(shooterSubsystem.setIndexerSpeedRunOnce(0), LEDSubsystem.Green()))
            .withName("INDEX NOTE WITH TOF");
    }

    public Command smartClimb() {
           return
            new InstantCommand(() -> shooterSubsystem.activateRatchet())
            .andThen(shooterSubsystem.climb())
            // .until(() -> {return shooterSubsystem.angleEncoder.getAbsolutePosition().getValue() < 0.03;})
            .finallyDo(() -> {shooterSubsystem.activateRatchet();})
            .withName("Climbing");
    }

    public Command smartIntake() {
        return 
            (shooterSubsystem.setShooterSpeedCommand(0.0))
                .andThen(
                new ParallelCommandGroup(
                (shooterSubsystem.setIndexerSpeed(1)),
                intakeSubsystem.setIntakeSpeed(0.8),
                new InstantCommand(() -> {shooterSubsystem.setAnglePosition(0.0225);}) // 0.0125
                ))
            .until(() -> {return shooterSubsystem.TOF.getRange() < 165;})
            .andThen(new InstantCommand(() -> {shooterSubsystem.load(0);}))
            .andThen(new InstantCommand(() -> {intakeSubsystem.load(0);}))
            .andThen(indexWithTOF())
            .finallyDo(() -> {
                intakeSubsystem.load(0);
                shooterSubsystem.setAnglePosition(0);
                if (shooterSubsystem.TOF.getRange() > 165){
                    LEDSubsystem.Red();
                } else if (shooterSubsystem.TOF.getRange() > 110){
                    // shooterSubsystem.lowerNote();
                    // LEDSubsystem.yellow();
                } else {
                    LEDSubsystem.Green();
                }
            });
        }

    public Command autoIntake() {
        return
            (shooterSubsystem.setShooterSpeedCommand(0.0))
                .andThen(
                new ParallelCommandGroup(
                (shooterSubsystem.setIndexerSpeed(1)),
                intakeSubsystem.setIntakeSpeed(0.8),
                new InstantCommand(() -> {shooterSubsystem.setAnglePosition(0.0225);}) // 0.0125
                ))
            .until(() -> {return shooterSubsystem.TOF.getRange() < 165;})
            .andThen(new InstantCommand(() -> {shooterSubsystem.load(0);}))
            .andThen(new InstantCommand(() -> {intakeSubsystem.load(0);}))
            .andThen(autoIndex())
            .finallyDo(() -> {
                intakeSubsystem.load(0);
                shooterSubsystem.setAnglePosition(0);
                if (shooterSubsystem.TOF.getRange() > 165){
                    LEDSubsystem.Red();
                } else if (shooterSubsystem.TOF.getRange() > 110){
                    // shooterSubsystem.lowerNote();
                    // LEDSubsystem.yellow();
                } else {
                    LEDSubsystem.Green();
                }
            });
        }

    public Command smartOuttake() {
        return  
            new ParallelCommandGroup(
                shooterSubsystem.setIndexerSpeed(-1),
                intakeSubsystem.setIntakeSpeed(-0.8),
                new InstantCommand(() -> {shooterSubsystem.setAnglePosition(0.0225);}) // 0.0125
                )
            .andThen(new InstantCommand(() -> {shooterSubsystem.load(0);}))
            .andThen(new InstantCommand(() -> {intakeSubsystem.load(0);}))
            .finallyDo(() -> {
                intakeSubsystem.load(0);
                shooterSubsystem.setAnglePosition(0);
                if (shooterSubsystem.TOF.getRange() > 165){
                    LEDSubsystem.Red();
                } else if (shooterSubsystem.TOF.getRange() > 110){
                    // shooterSubsystem.lowerNote();
                    // LEDSubsystem.yellow();
                } else {
                    LEDSubsystem.Green();
                }
            });
        }
        
    public Command runIntake() {
        return  
            intakeSubsystem.setIntakeSpeed(-1)
            .finallyDo(() -> {
                intakeSubsystem.load(0);
            });
        }

    public Command autoShooter() {
            return 
            shooterSubsystem.setShooterSpeed(1);
        }

    public Command autoIndexer() {
            return
            shooterSubsystem.setIndexerSpeed(0.8)
            .until(() -> {return shooterSubsystem.TOF.getRange() > 400;});
    }

    public Command stopShooter() {
            return
            shooterSubsystem.setShooterSpeedCommand(0.0);
    }        

    public Command stopIndexer() {
            return
            shooterSubsystem.setIndexerSpeedCommand(0.0);
    }

    public Command smartShoot() {
            return
            // indexWithTOF()
            (shooterSubsystem.setShooterSpeed(1))
            .andThen((shooterSubsystem.shootFromNotePosition()))
            .andThen(shooterSubsystem.setIndexerSpeed(0.8))
            .until(() -> {return shooterSubsystem.TOF.getRange() > 400;})
            .andThen(shooterSubsystem.setShooterSpeedCommand(0.0))
            .andThen(shooterSubsystem.setIndexerSpeedCommand(0.0));
        }

    public Command shootWithTOF() {
            return
            indexWithTOF()
            .andThen(shooterSubsystem.setShooterSpeed(1))
            .andThen(shooterSubsystem.setIndexerSpeed(0.8))
            .until(() -> {return shooterSubsystem.TOF.getRange() > 400;})
            .andThen(shooterSubsystem.setShooterSpeedCommand(0.0))
            .andThen(shooterSubsystem.setIndexerSpeedCommand(0.0));
        }
    
    public Command startShoot() {
            return
            new InstantCommand(() -> {
                if (shooterSubsystem.angleEncoder.getAbsolutePosition().getValueAsDouble() < 0.135) {
                    shooterSubsystem.setAnglePosition(0.015);//.015
                }
            })
            .andThen(shooterSubsystem.setShooterSpeed(1))
            .andThen(shooterSubsystem.setIndexerSpeed(0.8))
            .until(() -> {return shooterSubsystem.TOF.getRange() > 400;}) // 400
            .andThen(shooterSubsystem.setShooterSpeedCommand(0.0))
            .andThen(shooterSubsystem.setIndexerSpeedCommand(0.0));
        }

    public Command shootNoTOF() {
        return
            shooterSubsystem.setShooterSpeed(1)
            .andThen(shooterSubsystem.setIndexerSpeed(0.8));
    }

    public Command shootNoTOFWithTargetCheck() {
        if (s_Swerve.table.getEntry("tv").getInteger(0) == 1)
        {
            return
                shooterSubsystem.setShooterSpeed(1)
                .andThen(shooterSubsystem.setIndexerSpeed(0.8));
        } else {
            return shooterSubsystem.setShooterSpeed(0);
        }
       
    }

    public Command aimAndShoot() {
            return
            limeLightAutoAim().withTimeout(1)
            .andThen(shooterSubsystem.setShooterSpeed(1))
            .andThen(shooterSubsystem.setIndexerSpeed(0.8))
            .until(() -> {return shooterSubsystem.TOF.getRange() > 400;})
            .andThen(shooterSubsystem.setShooterSpeedCommand(0.0))
            .andThen(shooterSubsystem.setIndexerSpeedCommand(0.0));
        }
    
    public Command aimAndShootAuto() {
        return
        shooterSubsystem.setIndexerSpeed(-0.8) // -0.05
            .until(() -> {return shooterSubsystem.TOF.getRange() > 165;})
            .andThen(shooterSubsystem.setIndexerSpeedRunOnce(0))
       .andThen( limeLightAutoAim().withTimeout(.5))
        .andThen(shooterSubsystem.setShooterSpeed(1))
        .andThen(shooterSubsystem.setIndexerSpeed(1).withTimeout(0.2)) 
        .andThen(shooterSubsystem.setShooterSpeedCommand(0.0))
        .andThen(shooterSubsystem.setIndexerSpeedCommand(0.0));
    }

    public Command smartSecondNoteShoot() {
        return
        // indexWithTOF()
        (shooterSubsystem.setShooterSpeed(1))
        .andThen((shooterSubsystem.autoSecondNotePosition()))
        .andThen(shooterSubsystem.setIndexerSpeed(0.8))
        .until(() -> {return shooterSubsystem.TOF.getRange() > 400;})
        .andThen(shooterSubsystem.setShooterSpeedCommand(0.0))
        .andThen(shooterSubsystem.setIndexerSpeedCommand(0.0));
    }
    
    public Command smartShootThirdNote() {
        return
        (shooterSubsystem.setShooterSpeed(1))
        .andThen(shooterSubsystem.autoThirdNotePosition())
        .andThen(shooterSubsystem.setIndexerSpeed(0.8))
        .until(() -> {return shooterSubsystem.TOF.getRange() > 400;})
        .andThen(shooterSubsystem.setShooterSpeedCommand(0.0))
        .andThen(shooterSubsystem.setIndexerSpeedCommand(0.0));
    }

    public Command smartShootLastNote() {
        return
        (shooterSubsystem.setShooterSpeed(1))
        .andThen(shooterSubsystem.autoLastNotePosition())
        .andThen(shooterSubsystem.setIndexerSpeed(0.8));
        // .until(() -> {return shooterSubsystem.TOF.getRange() > 400;})
        // .andThen(shooterSubsystem.setShooterSpeedCommand(0.0))
        // .andThen(shooterSubsystem.setIndexerSpeedCommand(0.0));
    }

    public Command smartShaneNoteShoot() {
        return
        // indexWithTOF()
        (shooterSubsystem.setShooterSpeed(1))
        .andThen((shooterSubsystem.autoShaneNotePosition()))
        .andThen(shooterSubsystem.setIndexerSpeed(0.8))
        .until(() -> {return shooterSubsystem.TOF.getRange() > 400;})
        .andThen(shooterSubsystem.setShooterSpeedCommand(0.0))
        .andThen(shooterSubsystem.setIndexerSpeedCommand(0.0))
        .andThen(() -> {shooterSubsystem.setAnglePosition(0);});
    }

    public Command defaultShooterAngle() {
        return new InstantCommand(
        () -> {shooterSubsystem.setAnglePosition(0);});
    }


    // WIP limelight auto aiming DO NOT RUN
    //  step 1 get angle when at speaker
    //  step 2 is get angle at furthest shooting distance 
    //  if we are inbetween these then we can move shooter angle 
    //  calculate position to set shooter angle 
    //  if statement inside top conditional don't move shooter below 0 position 
    //  else set position 



    // public Command limelightAutoAimAlign() {
    //     return limeLightAutoAim().andThen(s_Swerve.limeLightAutoAlign());
    // }
    public Command limelightAimAndFire() {
        return limelightAutoAimAlign().withTimeout(0.75) // 0.75
            .andThen(shootNoTOF().withTimeout(0.5))
            .finallyDo(() -> {
                shooterSubsystem.setAnglePosition(0);
                shooterSubsystem.shoot(0.0);
                shooterSubsystem.load(0.0);
            });
    }

    // DO NOT USE FOR MATCHES
    public Command limelightAimAndFireDEBUG() {
        return limelightAutoAimAlign().withTimeout(0.75)
            .andThen(shootNoTOF().withTimeout(0.5))
            .finallyDo(() -> {
                //shooterSubsystem.setAnglePosition(0);
                shooterSubsystem.shoot(0.0);
                shooterSubsystem.load(0.0);
            })
            ;
    }

    public Command limelightAutoAimAlign() {
        return new ParallelCommandGroup(s_Swerve.limeLightAutoAlign(), limeLightAutoAim());
        // return s_Swerve.limeLightAutoAlign().andThen(limeLightAutoAim());
    }

    public Command limelightAutoAlignAndFireAtAmp() {
        return
        new ParallelCommandGroup(s_Swerve.limeLightAutoAlign(), shooterSubsystem.shootFromNotePosition()).withTimeout(0.5)
        //s_Swerve.limeLightAutoAlign().withTimeout(0.5)
        // .andThen(() -> {
        //     shooterSubsystem.setAnglePosition(.16);})
        // .until(() -> {return shooterSubsystem.angleEncoder.getAbsolutePosition().getValue() > 0.19;})
        //.andThen(shooterSubsystem.shootFromNotePosition())
        .andThen(shootNoTOF()) // .withTimeout(0.5))
        .finallyDo(() -> {
            shooterSubsystem.setAnglePosition(0);
            shooterSubsystem.shoot(0.0);
            shooterSubsystem.load(0.0);
        });
    }

    public Command limelightAutoStageAim() {
        return new ParallelCommandGroup(s_Swerve.limelightStageAlign(), shooterSubsystem.shootFromNotePosition());
    }

    public Command limeLightAutoAim() {
        return (new RunCommand(() -> {
        if (shooterSubsystem.calculateAngleToSpeaker() < 46.8 && shooterSubsystem.calculateAngleToSpeaker() > 15.89) { 
            double angleTargetAsPosition = Math.abs(shooterSubsystem.calculateAngleToSpeaker() - 54) * 0.00231214; // 51.623 // 54 and 0.00231214
            if (angleTargetAsPosition > 0) {
                shooterSubsystem.setAnglePosition(angleTargetAsPosition);
            }
        } //else {
        //     shooterSubsystem.setAnglePosition(0);
        // }
        })
        );
    }
    // Angle to goal on left, angle of shooter on right
    public double[][] lookUpTable = {
        {46.40, 0.00347}, 
        {43.90, 0.009}, 
        {41.90, 0.013},
        {39.90, 0.017},
        {37.70, 0.022},
        {35.87, 0.027}, 
        {34.55, 0.030}, 
        {33.18, 0.033},
        {32.12, 0.036},
        {30.70, 0.039},
        {29.52, 0.042}, 
        {28.45, 0.045}, 
        {27.29, 0.046},
        {26.56, 0.049},
        {25.66, 0.052},
        {24.92, 0.052}, 
        {24.19, 0.0615}, //.59
        {23.48, 0.055},
        {22.72, 0.058},
        {22.19, 0.058},
        {21.70, 0.060}, 
        {21.23, 0.066}, //.061
        {20.80, 0.061},
        {20.25, 0.059}
    };

    public Command limeLightAutoAimLookupTable() {
        return (new RunCommand(() -> {
            int i2 = 0;
            for(int i = 0; i < lookUpTable.length - 1; i++) {
                if (shooterSubsystem.calculateAngleToSpeaker() <= lookUpTable[i][0] && shooterSubsystem.calculateAngleToSpeaker() >= lookUpTable[i + 1][0]) {
                    i2 = i;
                    //shooterSubsystem.setAnglePosition(lookUpTable[i][1]);
                   // System.out.println(i2);
                   shooterSubsystem.setAnglePosition(lookUpTable[i2][1]);
                }
              //  System.out.println(i2 + 1);
            }
            

            // if (i2 > 0 && i2 < lookUpTable.length) {
            //     shooterSubsystem.setAnglePosition(lookUpTable[i2 - 1][1]);
            // }
            // else {
            //     shooterSubsystem.setAnglePosition(lookUpTable[i2][1]);
            // }
        })
        );
    }

    // public Command limeLightAutoAim() {
    //     return (new RunCommand(() -> {
    //     if (s_Swerve.calculateAngleToSpeaker() < 46.8 && s_Swerve.calculateAngleToSpeaker() > 20.89) { 
    //         double angleTargetAsPosition = Math.abs(s_Swerve.calculateAngleToSpeaker() - 50) * 0.00231214; // -50 and 0.00231214
    //         if (angleTargetAsPosition > 0) {
    //             shooterSubsystem.setAnglePosition(angleTargetAsPosition);
    //         }
    //     } //else {
    //     //     shooterSubsystem.setAnglePosition(0);
    //     // }
    //     })
    //     );
    // }

    /**
     * Use this to pass the autonomous command to the main {@link Robot} class.
     *
     * @return the command to run in autonomous
     */
    public Command getAutonomousCommand() {
        // return oneNoteAuto();
        // return new PathPlannerAuto("twoNoteAuto");
        // return followTrajectory(sTrajectory);
        return chooser.getSelected();
    }

    public Command followTrajectory(Trajectory trajectory) {
        ProfiledPIDController thetaController = new ProfiledPIDController(15, 0, 0, AutoConstants.kThetaControllerConstraints);
        thetaController.enableContinuousInput(-Math.PI, Math.PI);

        HolonomicDriveController controller = new HolonomicDriveController(
                new PIDController(1.125, 0, 0.1), 
                new PIDController(1.125, 0, 0.1), 
                thetaController
            );

        SwerveControllerCommand swerveControllerCommand =
            new SwerveControllerCommand(trajectory, s_Swerve::getPose, Constants.Swerve.swerveKinematics, controller, s_Swerve::setModuleStates, s_Swerve);
                
        return Commands.runOnce(
            () -> s_Swerve.setPose(trajectory.getInitialPose()))
            .andThen(swerveControllerCommand)
            .andThen(Commands.runOnce(() -> s_Swerve.drive(new Translation2d(0,0), 0, false, false)));
    }
        

    public Command oneNoteAuto() {
        return new InstantCommand(() -> {timer.restart();})
            .andThen(shooterSubsystem.setShooterSpeed(1))
            .until(() -> {return timer.get() > 1;})
            .andThen(shooterSubsystem.setIndexerSpeed(0.8))
            .until(() -> {return timer.get() > 2;})
            .andThen(shooterSubsystem.setShooterSpeedCommand(0.0))
            .andThen(shooterSubsystem.setIndexerSpeedCommand(0.0))
            // .andThen(transform())
            .until(() -> {return timer.get() > 4;});

    }

    public Command twoNoteAuto() {
        return new InstantCommand(() -> {timer.restart();})
            .andThen(shooterSubsystem.setShooterSpeed(1))
            // .until(() -> {return timer.get() > 1;})
            .andThen(shooterSubsystem.setIndexerSpeed(0.8))
            .until(() -> {return shooterSubsystem.TOF.getRange() > 400;})
            // .until(() -> {return timer.get() > 2;})
            .andThen(shooterSubsystem.setShooterSpeedCommand(0.0))
            .andThen(shooterSubsystem.setIndexerSpeedCommand(0.0))
            .andThen(transform())
            // .until(() -> {return timer.get() > 4;})
            .andThen(
                new PathPlannerAuto("twoNoteAuto")
                )
            // STOP MOVING
            .andThen(new InstantCommand(() -> {s_Swerve.drive(new Translation2d(0,0), 0, false, false);}))
            .andThen(shooterSubsystem.setShooterSpeed(1))
            .andThen(shooterSubsystem.setIndexerSpeed(0.8));
            // .until(() -> {return timer.get() > 10;})
            // .andThen(shooterSubsystem.setShooterSpeedCommand(0.0))
            // .andThen(shooterSubsystem.setIndexerSpeedCommand(0.0));
            // .until(() -> {return timer.get() > 10;})
            // .andThen(shooterSubsystem.setShooterSpeed(1))
            // .until(() -> {return timer.get() > 11;})
            // .andThen(shooterSubsystem.setIndexerSpeed(0.8))
            // .until(() -> {return timer.get() > 12;})
            // .andThen(shooterSubsystem.setShooterSpeedCommand(0.0))
            // .andThen(shooterSubsystem.setIndexerSpeedCommand(0.0));

    }

    public Command threeNoteAuto() {
        return
            (shooterSubsystem.setShooterSpeed(1))
            .andThen(shooterSubsystem.setIndexerSpeed(0.8))
            .until(() -> {return shooterSubsystem.TOF.getRange() > 400;})
            .andThen(shooterSubsystem.setShooterSpeedCommand(0.0))
            .andThen(shooterSubsystem.setIndexerSpeedCommand(0.0))
            .andThen(transform())
            .andThen(new PathPlannerAuto("threeNoteAuto"))
            .andThen(shooterSubsystem.shootFromNotePosition())
            .andThen(shooterSubsystem.setShooterSpeed(1))
            .andThen(shooterSubsystem.setIndexerSpeed(0.8))
            
            // STOP MOVING
            .andThen(new InstantCommand(() -> {s_Swerve.drive(new Translation2d(0,0), 0, false, false);}))
            .andThen(shooterSubsystem.setShooterSpeedCommand(0.0))
            .andThen(shooterSubsystem.setIndexerSpeedCommand(0.0));

    }

    public Command fourNoteAuto() {
        return
            (shooterSubsystem.setShooterSpeed(1))
            .andThen(shooterSubsystem.setIndexerSpeed(0.8))
            .until(() -> {return shooterSubsystem.TOF.getRange() > 400;})
            .andThen(shooterSubsystem.setShooterSpeedCommand(0.0))
            .andThen(shooterSubsystem.setIndexerSpeedCommand(0.0))
            .andThen(transform())
            .andThen(new PathPlannerAuto("fourNoteAuto"))
            .andThen(shooterSubsystem.autoLastNotePosition())
            .andThen(shooterSubsystem.setShooterSpeed(1))
            .andThen(shooterSubsystem.setIndexerSpeed(0.8))
            
            // STOP MOVING
            .andThen(new InstantCommand(() -> {s_Swerve.drive(new Translation2d(0,0), 0, false, false);}))
            .andThen(shooterSubsystem.setShooterSpeedCommand(0.0))
            .andThen(shooterSubsystem.setIndexerSpeedCommand(0.0));

    }

    public Command midfieldAuto() {
        return
            (shooterSubsystem.setShooterSpeed(1))
            .andThen(shooterSubsystem.setIndexerSpeed(0.8))
            .until(() -> {return shooterSubsystem.TOF.getRange() > 400;})
            .andThen(shooterSubsystem.setShooterSpeedCommand(0.0))
            .andThen(shooterSubsystem.setIndexerSpeedCommand(0.0))
            .andThen(transform())
            .andThen(new PathPlannerAuto("4NoteMidLoadSideAuto"))
            .andThen(shooterSubsystem.autoLastNotePosition())
            .andThen(shooterSubsystem.setShooterSpeed(1))
            .andThen(shooterSubsystem.setIndexerSpeed(0.8))
            
            // STOP MOVING
            .andThen(new InstantCommand(() -> {s_Swerve.drive(new Translation2d(0,0), 0, false, false);}))
            .andThen(shooterSubsystem.setShooterSpeedCommand(0.0))
            .andThen(shooterSubsystem.setIndexerSpeedCommand(0.0));

    }

    public Command midfield() {
        return
            (shooterSubsystem.setShooterSpeed(1))
            .andThen(shooterSubsystem.setIndexerSpeed(0.8))
            .until(() -> {return shooterSubsystem.TOF.getRange() > 400;})
            .andThen(shooterSubsystem.setShooterSpeedCommand(0.0))
            .andThen(shooterSubsystem.setIndexerSpeedCommand(0.0))
            .andThen(transform())
            .andThen(new PathPlannerAuto("midfield1Auto"))
            .andThen(shooterSubsystem.autoLastNotePosition())
            .andThen(shooterSubsystem.setShooterSpeed(1))
            .andThen(shooterSubsystem.setIndexerSpeed(0.8))
            .andThen(new PathPlannerAuto("midfield2Auto"))
            .andThen(shooterSubsystem.autoLastNotePosition())
            .andThen(shooterSubsystem.setShooterSpeed(1))
            .andThen(shooterSubsystem.setIndexerSpeed(0.8))
            .andThen(new PathPlannerAuto("midfield3Auto"))
            .andThen(shooterSubsystem.autoLastNotePosition())
            .andThen(shooterSubsystem.setShooterSpeed(1))
            .andThen(shooterSubsystem.setIndexerSpeed(0.8))
            
            // STOP MOVING
            .andThen(new InstantCommand(() -> {s_Swerve.drive(new Translation2d(0,0), 0, false, false);}))
            .andThen(shooterSubsystem.setShooterSpeedCommand(0.0))
            .andThen(shooterSubsystem.setIndexerSpeedCommand(0.0));

    }

    public Command newMidfieldAuto() {
        return new PathPlannerAuto("midfieldLoadSideAuto")
        .andThen(new InstantCommand(() -> {s_Swerve.drive(new Translation2d(0,0), 0, false, false);}))
        .andThen(shooterSubsystem.setShooterSpeedCommand(0.0))
        .andThen(shooterSubsystem.setIndexerSpeedCommand(0.0));
    }

    public Command rightMidfieldAuto() {
        return new InstantCommand(() -> {timer.restart();})
            .andThen(shooterSubsystem.setShooterSpeed(1))
            .until(() -> {return timer.get() > 1;})
            .andThen(shooterSubsystem.setIndexerSpeed(0.8))
            .until(() -> {return timer.get() > 2;})
            .andThen(shooterSubsystem.setShooterSpeedCommand(0.0))
            .andThen(shooterSubsystem.setIndexerSpeedCommand(0.0))
            .andThen(new PathPlannerAuto("midfieldAuto"))
            .andThen(shooterSubsystem.setShooterSpeed(1))
            .andThen(shooterSubsystem.setIndexerSpeed(0.8));
            // .andThen(shooterSubsystem.setShooterSpeedCommand(0.0))
            // .andThen(shooterSubsystem.setIndexerSpeedCommand(0.0));
    }

  /*   public Command newFourPieceAuto() {
        return new PathPlannerAuto("newFourNoteAuto");
    } */

   /*  public Command altFourPieceAuto() {
        return new PathPlannerAuto("FourNoteSpeakerAuto");
    } */

    public Command MidSpeaker5NoteAuto() {
        return new PathPlannerAuto("MidSpeaker5NoteAuto");
    }

    public Command MidSpeaker5NoteAutoBlue() {
        return new PathPlannerAuto("MidSpeaker5NoteAutoBlue");
    }
    
   /*  public Command quickTest() {
        return new PathPlannerAuto("quickTest");
    } */

    public Command MidSpeaker4NoteAuto() {
        return new PathPlannerAuto("MidSpeaker4NoteAuto");
    }

    public Command fivebutbetterauto() {
       return new PathPlannerAuto("fivebutbetterauto");
    }

    public Command crazyAuto() {
       return new PathPlannerAuto("crazyAuto");
    }

    public Command midAuto() {
       return new PathPlannerAuto("midAuto");
    }

    public Command oldeMidAuto() {
       return new PathPlannerAuto("midfieldAuto");
    }

    public Command outsideAuto() {
       return new PathPlannerAuto("outsideAuto");
    }

    public Command midTestAuto() {
        return new PathPlannerAuto("TESTfivebutbetterauto");
    }
public Command ampSideAuto() {
        return new PathPlannerAuto("ampSideAuto");
    }

    public Command fourNoteTest() {
        return new PathPlannerAuto("4testauto");
    }

    public Command pidTest() {
        return new PathPlannerAuto("PIDTest");
    }

    public Command ChoreoTest() {
        return new PathPlannerAuto("ChoreoTest");
    }

    public Command Mid5NoteChoreo() {
        return
            (shooterSubsystem.setShooterSpeed(1))
            .andThen(shooterSubsystem.setIndexerSpeed(0.8))
            .until(() -> {return shooterSubsystem.TOF.getRange() > 400;})
            .andThen(shooterSubsystem.setShooterSpeedCommand(0.0))
            .andThen(shooterSubsystem.setIndexerSpeedCommand(0.0))
            .andThen(transform())
            .andThen(new PathPlannerAuto("Mid5NoteChoreo"))
            .andThen(shooterSubsystem.setShooterSpeed(1))
            .andThen(shooterSubsystem.setIndexerSpeed(0.8));

    }
    public Command Mid4NoteFinal() {
        return
            (shooterSubsystem.setShooterSpeed(1))
            .andThen(shooterSubsystem.setIndexerSpeed(0.8))
            .until(() -> {return shooterSubsystem.TOF.getRange() > 400;})
            .andThen(shooterSubsystem.setShooterSpeedCommand(0.0))
            .andThen(shooterSubsystem.setIndexerSpeedCommand(0.0))
            .andThen(transform())
            .andThen(new PathPlannerAuto("Mid4NoteFinal"))
            .andThen(shooterSubsystem.setShooterSpeed(1))
            .andThen(shooterSubsystem.setIndexerSpeed(0.8));

    }
    public Command Outside3NoteChoreo() {
        return
            (shooterSubsystem.setShooterSpeed(1))
            .andThen(shooterSubsystem.setIndexerSpeed(0.8))
            .until(() -> {return shooterSubsystem.TOF.getRange() > 400;})
            .andThen(shooterSubsystem.setShooterSpeedCommand(0.0))
            .andThen(shooterSubsystem.setIndexerSpeedCommand(0.0))
            .andThen(transform())
            .andThen(new PathPlannerAuto("Outside3NoteChoreo"))
            .andThen(shooterSubsystem.setShooterSpeed(1))
            .andThen(shooterSubsystem.setIndexerSpeed(0.8));

    }

    public Command BetterOutside() {
        return
            (shooterSubsystem.setShooterSpeed(1))
            .andThen(shooterSubsystem.setIndexerSpeed(0.8))
            .until(() -> {return shooterSubsystem.TOF.getRange() > 400;})
            .andThen(shooterSubsystem.setShooterSpeedCommand(0.0))
            .andThen(shooterSubsystem.setIndexerSpeedCommand(0.0))
            .andThen(transform())
            .andThen(new PathPlannerAuto("BetterOutside"))
            .andThen(shooterSubsystem.setShooterSpeed(1))
            .andThen(shooterSubsystem.setIndexerSpeed(0.8));

    }

/* 
    // simple proportional turning control with Limelight.
    // "proportional control" is a control algorithm in which the output is proportional to the error.
    // in this case, we are going to return an angular velocity that is proportional to the 
    // "tx" value from the Limelight.
    double limelight_aim_proportional()
    {    
    // kP (constant of proportionality)
    // this is a hand-tuned number that determines the aggressiveness of our proportional control loop
    // if it is too high, the robot will oscillate around.
    // if it is too low, the robot will never reach its target
    // if the robot never turns in the correct direction, kP should be inverted.
    double kP = .035;

    // tx ranges from (-hfov/2) to (hfov/2) in degrees. If your target is on the rightmost edge of 
    // your limelight 3 feed, tx should return roughly 31 degrees.
    double targetingAngularVelocity = s_Swerve.table.getTX("limelight") * kP;

    // convert to radians per second for our drive method
    targetingAngularVelocity *= Constants.Swerve.kMaxAngularSpeedRadiansPerSecond;

    //invert since tx is positive when the target is to the right of the crosshair
    targetingAngularVelocity *= -1.0;

    return targetingAngularVelocity;
  }

  // simple proportional ranging control with Limelight's "ty" value
  // this works best if your Limelight's mount height and target mount height are different.
  // if your limelight and target are mounted at the same or similar heights, use "ta" (area) for target ranging rather than "ty"
  double limelight_range_proportional()
  {    
    double kP = .1;
    double targetingForwardSpeed = LimelightHelpers.getTY("limelight") * kP;
    targetingForwardSpeed *= Constants.Swerve.maxSpeed;
    targetingForwardSpeed *= -1.0;
    return targetingForwardSpeed;
  }

  private void drive(boolean fieldRelative) {
    // Get the x speed. We are inverting this because Xbox controllers return
    // negative values when we push forward.
    var xSpeed =
        -m_xspeedLimiter.calculate(MathUtil.applyDeadband(m_controller.getLeftY(), 0.02))
            * Constants.Swerve.maxSpeed;

    // Get the y speed or sideways/strafe speed. We are inverting this because
    // we want a positive value when we pull to the left. Xbox controllers
    // return positive values when you pull to the right by default.
    var ySpeed =
        -m_yspeedLimiter.calculate(MathUtil.applyDeadband(m_controller.getLeftX(), 0.02))
            * Constants.Swerve.maxSpeed;

    // Get the rate of angular rotation. We are inverting this because we want a
    // positive value when we pull to the left (remember, CCW is positive in
    // mathematics). Xbox controllers return positive values when you pull to
    // the right by default.
    var rot =
        -m_rotLimiter.calculate(MathUtil.applyDeadband(m_controller.getRightX(), 0.02))
            * Drivetrain.kMaxAngularSpeed;

    // while the A-button is pressed, overwrite some of the driving values with the output of our limelight methods
    if(driver.y().getAsBoolean() == true)
    {
        final var rot_limelight = limelight_aim_proportional();
        rot = rot_limelight;

        final var forward_limelight = limelight_range_proportional();
        xSpeed = forward_limelight;

        //while using Limelight, turn off field-relative driving.
        fieldRelative = false;
    }

    s_Swerve.drive(xSpeed, ySpeed, rot, fieldRelative, getPeriod());
  }
*/
}