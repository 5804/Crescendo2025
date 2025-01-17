// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

// import com.ctre.phoenix.motorcontrol.can.TalonFX;
// import com.ctre.phoenix.music.Orchestra;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.net.PortForwarder;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.PWM;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.subsystems.LED;


/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  public static final CTREConfigs ctreConfigs = new CTREConfigs();

  private Command m_autonomousCommand;

  private RobotContainer m_robotContainer;

  private final LED LEDSubsystem = new LED();

  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    // Instantiate our RobotContainer.  This will perform all our button bindings, and put our
    // autonomous chooser on the dashboard.
    m_robotContainer = new RobotContainer();
    CameraServer.startAutomaticCapture();
    CameraServer.getVideo();

    LEDSubsystem.Off();

    //Orchestra m_orchestra = new Orchestra();

    // m_orchestra.addInstrument(RobotContainer.shooterSubsystem.leftShooterMotor);

    // var status = m_orchestra.loadMusic("");

    // if (!status.isOK()) {
    //     m_orchestra.play();
    // }

    // adding port forwarding for limelights
    for (int port = 5800; port <= 5807; port++) {
      PortForwarder.add(port, "limelight.local", port);
    }

        //LEDSubsystem.SetAnimationFire();
  }

  /**
   * This function is called every robot packet, no matter the mode. Use this for items like
   * diagnostics that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
    // Runs the Scheduler.  This is responsible for polling buttons, adding newly-scheduled
    // commands, running already-scheduled commands, removing finished or interrupted commands,
    // and running subsystem periodic() methods.  This must be called from the robot's periodic
    // block in order for anything in the Command-based framework to work.
    CommandScheduler.getInstance().run();
  }

   
  /** This function is called once each time the robot enters Disabled mode. */
  @Override

 
  public void disabledInit() {
      // LEDSubsystem.();
  }

  @Override
  public void disabledPeriodic() {
    
  }

  /** This autonomous runs the autonomous command selected by your {@link RobotContainer} class. */
  @Override
  public void autonomousInit() {
    Robot.ctreConfigs.swerveDriveFXConfig.Slot0.kP = 1.5; // 1.5 for pathplanner
    for (int i=0; i<m_robotContainer.s_Swerve.mSwerveMods.length; i++) {
      m_robotContainer.s_Swerve.mSwerveMods [i].mDriveMotor.getConfigurator().apply(Robot.ctreConfigs.swerveDriveFXConfig);
    }

    m_robotContainer.shooterSubsystem.deactivateRatchet();
    m_autonomousCommand = m_robotContainer.getAutonomousCommand();

    // schedule the autonomous command (example)
    if (m_autonomousCommand != null) {
      m_autonomousCommand.schedule();
    }

    LEDSubsystem.Green();
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {}

  @Override
  public void teleopInit() {
    // This makes sure that the autonomous stops running when
    // teleop starts running. If you want the autonomous to
    // continue until interrupted by another command, remove
    // this line or comment it out.
    if (m_autonomousCommand != null) {
      m_autonomousCommand.cancel();
    }
    m_robotContainer.shooterSubsystem.deactivateRatchet();
    Robot.ctreConfigs.swerveDriveFXConfig.Slot0.kP = 3.59;
    for (int i=0; i<m_robotContainer.s_Swerve.mSwerveMods.length; i++) {
      m_robotContainer.s_Swerve.mSwerveMods [i].mDriveMotor.getConfigurator().apply(Robot.ctreConfigs.swerveDriveFXConfig);
    }
    // swerveDriveFXConfig.Slot0.kP = Constants.Swerve.driveKP;

    LEDSubsystem.Off();
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {}

  @Override
  public void testInit() {
    // Cancels all running commands at the start of test mode.
    CommandScheduler.getInstance().cancelAll();
  }

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {}
}
