// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

// import com.ctre.phoenix.music.Orchestra;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.Follower;
import com.ctre.phoenix6.controls.MotionMagicVoltage;
import com.ctre.phoenix6.hardware.CANcoder;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.FeedbackSensorSourceValue;
import com.ctre.phoenix6.signals.GravityTypeValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import com.playingwithfusion.TimeOfFlight;
import com.playingwithfusion.TimeOfFlight.RangingMode;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.PWM;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.PIDCommand;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.Robot;

public class Shooter extends SubsystemBase {

  public TalonFX rightShooterMotor;
  public TalonFX leftShooterMotor;
  public TalonFX indexerMotor;
  public TalonFX rightShooterAngleMotor;
  public TalonFX leftShooterAngleMotor;
  public CANcoder angleEncoder;
  public Rotation2d angleOffset;
  public TimeOfFlight TOF;
  public PWM ratchet;
  public NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight-vision");

  /** Creates a new Shooter. */
  public Shooter() {

    angleEncoder = new CANcoder(53);
    rightShooterMotor = new TalonFX(52);
    leftShooterMotor = new TalonFX(51);
    indexerMotor = new TalonFX(54);
    rightShooterAngleMotor = new TalonFX(61);
    leftShooterAngleMotor = new TalonFX(60);
    leftShooterMotor.setInverted(true);

    TOF = new TimeOfFlight(3);
    TOF.setRangingMode(RangingMode.Short, 30);
    ratchet = new PWM(2);

    var talonFXConfigs = new TalonFXConfiguration();
    talonFXConfigs.Feedback.FeedbackRemoteSensorID = angleEncoder.getDeviceID();
    talonFXConfigs.Feedback.FeedbackSensorSource = FeedbackSensorSourceValue.RemoteCANcoder;

    // Regular PIDs
    var slot0Configs = talonFXConfigs.Slot0;
    slot0Configs.GravityType = GravityTypeValue.Arm_Cosine;
    slot0Configs.kV = 7; // 3 // 8
    slot0Configs.kP = 45; // 40 // 35 // 45
    slot0Configs.kI = 0; // 0
    slot0Configs.kD = 0; // 0 // WIP VALUE
    slot0Configs.kS = 0; // 0

    // Climb PIDs
    var slot1Configs = talonFXConfigs.Slot1;
    slot1Configs.GravityType = GravityTypeValue.Arm_Cosine;
    slot1Configs.kV = 3; // 3
    slot1Configs.kP = 10; // 40 // 35 // 10
    slot1Configs.kI = 0; // 0
    slot1Configs.kD = 0; // 0
    slot1Configs.kS = 0; // 0
    

    var motionMagicConfigs = talonFXConfigs.MotionMagic;
    motionMagicConfigs.MotionMagicCruiseVelocity = 4.5; // 3 // 4.5
    motionMagicConfigs.MotionMagicAcceleration = 4; // 3 // 4
    motionMagicConfigs.MotionMagicJerk = 0; // 0

    leftShooterAngleMotor.getConfigurator().apply(talonFXConfigs, 0.050);
    rightShooterAngleMotor.getConfigurator().apply(talonFXConfigs, 0.050);

    rightShooterAngleMotor.setNeutralMode(NeutralModeValue.Brake);
    leftShooterAngleMotor.setNeutralMode(NeutralModeValue.Brake);
    rightShooterAngleMotor.setInverted(true);
    leftShooterAngleMotor.setInverted(true);
    indexerMotor.setNeutralMode(NeutralModeValue.Brake);
    leftShooterMotor.setNeutralMode(NeutralModeValue.Brake);
    rightShooterMotor.setNeutralMode(NeutralModeValue.Brake);

    rightShooterAngleMotor.setControl(new Follower(leftShooterAngleMotor.getDeviceID(), true));


  }
/* 
  //not sure if we need this
 public Rotation2d getCANcoder(){
        return Rotation2d.fromRotations(angleEncoder.getAbsolutePosition().getValue());
    }
  //and this
    public void resetToAbsolute(){
      double absolutePosition = getCANcoder().getRotations() - angleOffset.getRotations();

  }
*/
  public void shoot(double shootSpeed) {
    rightShooterMotor.set(shootSpeed);
    leftShooterMotor.set(shootSpeed);
    //intakeShooterMotor.set(speed);

  }

  public void load(double indexerSpeed) {
    indexerMotor.set(indexerSpeed);
  }

   public void lowerNote() {
    StatusSignal<Angle> pos = indexerMotor.getPosition();
    indexerMotor.setPosition(pos.getValueAsDouble() -2);
  }

  boolean enableShooter = true;
  public Command setShooterSpeedCommand(double shooterSpeed) {
    return runOnce(
            () -> {
              if (enableShooter == true) {
                shoot(shooterSpeed); 
                enableShooter = false;
              } else {
                shoot(0);
                enableShooter = true;
              }
            })
        .withName("SetShooterSpeed");
  }

  // SHANE PLAY WITH THIS NUMBER TOO
  public Command setShooterSpeed(double shooterSpeed) {
    return run(
            () -> {
                shoot(shooterSpeed);
            })
        .until(() -> { return rightShooterMotor.getVelocity().getValueAsDouble() > 25;}) //.97
        .withName("SetShooterSpeed RUN");
  }


  public Command setShooterSpeedRunOnce(double shooterSpeed) {
    return runOnce(
            () -> {
                shoot(shooterSpeed);
            })
        .withName("SetShooterSpeed RUN");
  }

/*
  public Command enableShooter() {
    return runOnce(
            () -> {
              shoot(shooterSpeed);
            })
        .withName("DisableShooter");
  }

  public Command disableShooter() {
    return runOnce(
            () -> {
              shoot(0);
            })
        .withName("DisableShooter");
  }
*/
  boolean indexerEnable = true;
  public Command setIndexerSpeedCommand(double indexerSpeed) {
    return runOnce(
            () -> {
              if (indexerEnable == true) {
                load(indexerSpeed); 
                indexerEnable = false;
              } else {
                load(0);
                indexerEnable = true;
              }
            })
        .withName("Load");
  }

  public Command setIndexerSpeed(double indexerSpeed) {
    return run(
      () -> {
        load(indexerSpeed);
      }
    ).finallyDo(() -> {load(0);}
    );
  }

  public Command indexDefaultCommand(double indexerSpeed) {
    return run(
      () -> {
        if (TOF.getRange() > 165) {
        load(indexerSpeed);
        }
      }
    ).finallyDo(() -> {load(0);}
    );
  }

  public Command setIndexerSpeedNoFinallyDo(double indexerSpeed) {
    return run(
      () -> {
        load(indexerSpeed);
      }
    );
  }

  public Command setIndexerSpeedRunOnce(double indexerSpeed) {
    return runOnce(
      () -> {
        load(indexerSpeed);
      }
    );
  }

  public void setAngleSpeed(double angleSpeed) {
    leftShooterAngleMotor.set(angleSpeed);
  }

  public double currentPosition = 0;
  public void setAnglePosition(double anglePosition) {
        currentPosition = anglePosition;

        MotionMagicVoltage request = new MotionMagicVoltage(0);
        leftShooterAngleMotor.setControl(request.withPosition(anglePosition));
      }

  public Command increaseShooterPos() {
    return run(
      () -> {
        setAnglePosition(currentPosition + 0.001);
      }
    // ).finallyDo(() -> {setAnglePosition(currentPosition);}
    );
  }

  public Command decreaseShooterPos() {
    return run(
      () -> {
        setAnglePosition(currentPosition - 0.001);
      }
    // ).finallyDo(() -> {setAnglePosition(currentPosition);}
    );
  }

    //calculate angle to speaker
    // how many degrees back is your limelight rotated from perfectly vertical?
    double limelightMountAngleDegrees = 31; 

    // distance from the center of the Limelight lens to the floor
    double limelightLensHeightInches = 10.25; 

    // distance from the target to the floor
    public double goalHeightInches = 53.875;
    public double calculateAngleToSpeaker() { 
        NetworkTableEntry ty = table.getEntry("ty");
        double targetOffsetAngle_Vertical = ty.getDouble(0.0);
        double angleToGoalDegrees = limelightMountAngleDegrees + targetOffsetAngle_Vertical;
        //double angleToGoalRadians = angleToGoalDegrees * (3.14159 / 180.0);
        return angleToGoalDegrees;
    }

  public void activateRatchet() {
    ratchet.setPosition(0);
  }

  public void deactivateRatchet() {
    ratchet.setPosition(1);
  }

      public Command stow() {
        return runOnce(
            () -> {
                setAnglePosition(0); // 0
            }
        );
        // .finallyDo(
        //     () -> {
        //         setAngleSpeed(0);
        //     }
        // );
      }

      // SHANE CHANGE THIS VALUE // OR DONT
      public Command shootFromNotePosition() {
        return run(
            () -> {
                setAnglePosition(.16); // 0.06
            }
        ).until(() -> {return angleEncoder.getAbsolutePosition().getValueAsDouble() > 0.15;});
      }

      public Command smartIntakePositionCommand() {
        return run(
            () -> {
                setAnglePosition(.0225); // 0.06
            }
        ).until(() -> {return angleEncoder.getAbsolutePosition().getValueAsDouble() > 0.0175 && angleEncoder.getAbsolutePosition().getValueAsDouble() < 0.0275;});
      }

      public Command autoLastNotePosition() {
        return run(
            () -> {
                setAnglePosition(.0598); // .051 // .062
            }
        ).until(() -> {return angleEncoder.getAbsolutePosition().getValueAsDouble() > 0.033;});
      }

      public Command autoThirdNotePosition() {
        return run(
            () -> {
                setAnglePosition(.060); // .043
            }
        ).until(() -> {return angleEncoder.getAbsolutePosition().getValueAsDouble() > 0.040;});
      }

      public Command autoSecondNotePosition() {
        return run(
            () -> {
                setAnglePosition(.0578); // .058
            }
        ).until(() -> {return angleEncoder.getAbsolutePosition().getValueAsDouble() > 0.033;});
      }

      public Command autoShaneNotePosition() {
        return run(
            () -> {
                setAnglePosition(.078); // .078
            }
        ).until(() -> {return angleEncoder.getAbsolutePosition().getValueAsDouble() > 0.06;});
      }

      public Command climb() {
        return run(
            () -> {
              MotionMagicVoltage request = new MotionMagicVoltage(0);
              request.withSlot(1);
              setAnglePosition(0.0);
            }
        );
      }

      public Command climbPosition() {
        return run(
            () -> {
                setAnglePosition(0.32); // Change this later
            }
        );
      }

      public Command amp() {
        return run(
            () -> {
                setAnglePosition(0.255);
            }
        )
        .finallyDo(
            () -> {
                setAngleSpeed(0);
            }
        );
      }

      public Command deploy() {
        return run(
            () -> {
                setAnglePosition(0.135); // 0.14
            }
        );
        // .finallyDo(
        //     () -> {
        //         setAngleSpeed(0);
        //     }
        // );
      }

      // public Command smartIntakePositionCommand() {
      //   return run(
      //       () -> {
      //           setAnglePosition(0.0225); // 0.06
      //       }
      //   ).until(() -> {return angleEncoder.getAbsolutePosition().getValue() > 0.020;});
      // }


      public boolean hasNote() {
        // double range = TOF.getRange();
        // return range < 20;
        return true;
      }

/* // Shooter speed adjust buttons
  public Command increaseShooterSpeed() { // Increase and decrease speed commands are temporary features for debugging
    return runOnce(
      () -> {
        if (shooterSpeed < 1) {
          shooterSpeed += .1;
          System.out.println("Current shooter speed: " + shooterSpeed);
          if (enableShooter == false) {
            shoot(shooterSpeed);
          }
        }
      })
      .withName("IncreaseShooterSpeed");
  }

  public Command decreaseShooterSpeed() {
    return runOnce(
      () -> {
        if (shooterSpeed > 0) {
          shooterSpeed -= .1;
          System.out.println("Current shooter speed: " + shooterSpeed);
          if (enableShooter == false) {
            shoot(shooterSpeed);
          }
        }
      })
      .withName("DecreaseShooterSpeed");
  }
*/
  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    Rotation2d rotations = Rotation2d.fromRotations(angleEncoder.getAbsolutePosition().getValueAsDouble());
    Rotation2d motorRotations = Rotation2d.fromRotations(leftShooterAngleMotor.getPosition().getValueAsDouble());
    SmartDashboard.putNumber("ShooterAngle", rotations.getDegrees());
    // double motorVelocity = rightShooterAngleMotor.getVelocity().getValue();
    SmartDashboard.putNumber("ShooterIntakeAngle", rotations.getDegrees());
    SmartDashboard.putNumber("ShooterIntakeAngleEncoder", motorRotations.getDegrees());
    SmartDashboard.putNumber("ShooterMotorVelocity", rightShooterAngleMotor.getVelocity().getValueAsDouble());
    SmartDashboard.putNumber("TimeOfFlightSensor", TOF.getRange());
    SmartDashboard.putNumber("RatchetPosition", rotations.getDegrees());
    SmartDashboard.putNumber("Angle to goal", calculateAngleToSpeaker());
  }

}