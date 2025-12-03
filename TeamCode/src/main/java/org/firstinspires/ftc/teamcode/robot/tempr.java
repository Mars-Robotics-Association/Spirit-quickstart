package org.firstinspires.ftc.teamcode.robot;
@TeleOp(name="TriggerStateMachine")
public class TriggerStateMachine extends OpMode {

    private DcMotor dcMotor;
    private DcMotor gobildaMotor;
    private Servo servo;
    private Servo gobildaServo;

    // State variables
    private enum State {
        IDLE,
        RIGHT_TRIGGER_RUNNING,
        LEFT_TRIGGER_RUNNING
    }

    private State currentState = State.IDLE;
    private double stateStartTime = 0;

    @Override
    public void init() {
        dcMotor      = hardwareMap.get(DcMotor.class, "dcMotor");
        gobildaMotor = hardwareMap.get(DcMotor.class, "gobildaMotor");
        servo        = hardwareMap.get(Servo.class, "servo");
        gobildaServo = hardwareMap.get(Servo.class, "gobildaServo");
    }

    @Override
    public void loop() {

        switch (currentState) {

            case IDLE:
                // Default motor state
                dcMotor.setPower(0);

                // Start RIGHT sequence
                if (gamepad1.right_trigger > 0.1) {
                    dcMotor.setPower(1.0);           // Start action
                    currentState = State.RIGHT_TRIGGER_RUNNING;
                    stateStartTime = runtime.seconds();
                }

                // Start LEFT sequence
                else if (gamepad1.left_trigger > 0.1) {
                    dcMotor.setPower(0.5);           // Start action
                    currentState = State.LEFT_TRIGGER_RUNNING;
                    stateStartTime = runtime.seconds();
                }

                break;

            case RIGHT_TRIGGER_RUNNING:
                // Has 2 seconds passed?
                if (runtime.seconds() - stateStartTime >= 2.0) {
                    dcMotor.setPower(0.2);
                    servo.setPosition(0.25);
                    currentState = State.IDLE;       // Reset to idle state
                }
                break;

            case LEFT_TRIGGER_RUNNING:
                // Has 2 seconds passed?
                if (runtime.seconds() - stateStartTime >= 2.0) {
                    gobildaMotor.setPower(0.6);
                    gobildaServo.setPosition(0.6);
                    currentState = State.IDLE;       // Reset to idle state
                }
                break;
        }
    }
}


