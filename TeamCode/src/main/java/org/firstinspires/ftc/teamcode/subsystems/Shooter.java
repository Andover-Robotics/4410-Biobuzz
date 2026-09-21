package org.firstinspires.ftc.teamcode.subsystems;


import com.qualcomm.hardware.modernrobotics.comm.RobotUsbDevicePretendModernRobotics;
import com.seattlesolvers.solverslib.controller.PIDController;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;

public class Shooter {
    public static double kP, kI,kD, f;
    public static double toleranceRPM = 75;
    public final double ticksPerRev = 28;
    private MotorEx motor;
    private MotorEx motor2;

    boolean rightEnc=true;

    private double filteredRPM, targetRPM;

    public final PIDController controller;



    protected boolean inRange() {
        return (filteredRPM > targetRPM - toleranceRPM && filteredRPM < targetRPM + toleranceRPM);
    }



    public Shooter(Bot bot, String name){
        motor = new MotorEx(bot.hardwareMap, name);
        motor2 = new MotorEx(bot.hardwareMap, name);
        motor2.setInverted(true);
        controller = new PIDController(kP,kI,kD);
        controller.setTolerance(toleranceRPM);
    }

    public void setTargetVel(int rpm){
        controller.setSetPoint(rpm);
        targetRPM = rpm;
    }

    private void setPower(double pow){
        motor.set(pow);
        motor2.set(pow);
    }



    public void periodic(){

        double velocity = rightEnc ? (motor.getVelocity() * 60) / ticksPerRev : (motor2.getVelocity()*60)/ticksPerRev;//tpm -> -> *60 /28 ticks per rev = rev/min
        setTargetVel(1);//replace with interpolator logic
        double pid = controller.calculate(velocity);
        double ff = targetRPM*f;
        double power = Bot.clamp(pid+ff,0,1);
        setPower(power);






    }







}
