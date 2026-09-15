package Commands;

public class GripperFeedback
{
    private String rotorStatus;
    private String rotorReason;
    private Boolean rotorLimitSwitchRight;
    private int rotorPosition;

    private String armStatus;
    private String armReason;
    private Boolean armLimitSwitchHit;
    private int armPosition;

    private String verticalStatus;
    private String verticalReason;
    private Boolean verticalLimitSwitchHit;
    private int verticalPosition;

    private String clawStatus;
    private String clawReason;
    private Boolean clawLimitSwitchHit;
    private int clawPosition;

    public  GripperFeedback(String rotorStatus, String rotorReason, Boolean rotorLimitSwitchRight, int rotorPosition,
                            String armStatus, String armReason, Boolean armLimitSwitchHit, int armPosition,
                            String verticalStatus, String verticalReason, Boolean verticalLimitSwitchHit, int verticalPosition,
                            String clawStatus, String clawReason, Boolean clawLimitSwitchHit, int clawPosition)
    {
        this.rotorStatus = rotorStatus;
        this.rotorReason = rotorReason;
        this.rotorLimitSwitchRight = rotorLimitSwitchRight;
        this.rotorPosition = rotorPosition;

        this.armStatus = armStatus;
        this.armReason = armReason;
        this.armLimitSwitchHit = armLimitSwitchHit;
        this.armPosition = armPosition;

        this.verticalStatus = verticalStatus;
        this.verticalReason = verticalReason;
        this.verticalLimitSwitchHit = verticalLimitSwitchHit;
        this.verticalPosition = verticalPosition;

        this.clawStatus = clawStatus;
        this.clawReason = clawReason;
        this.clawLimitSwitchHit = clawLimitSwitchHit;
        this.clawPosition = clawPosition;
    }

    public String getRotorStatus()
    {
        return rotorStatus;
    }
    public String getRotorReason()
    {
        return rotorReason;
    }
    public Boolean getRotorLimitSwitchRight()
    {
        return rotorLimitSwitchRight;
    }
    public int getRotorPosition()
    {
        return rotorPosition;
    }

    public String getArmStatus()
    {
        return armStatus;
    }
    public String getArmReason()
    {
        return armReason;
    }
    public Boolean getArmLimitSwitchHit()
    {
        return armLimitSwitchHit;
    }
    public int getArmPosition()
    {
        return armPosition;
    }

    public String getVerticalStatus()
    {
        return verticalStatus;
    }
    public String getVerticalReason()
    {
        return verticalReason;
    }
    public Boolean getVerticalLimitSwitchHit()
    {
        return verticalLimitSwitchHit;
    }
    public int getVerticalPosition()
    {
        return verticalPosition;
    }

    public String getClawStatus()
    {
        return clawStatus;
    }
    public String getClawReason()
    {
        return clawReason;
    }
    public Boolean getClawLimitSwitchHit()
    {
        return clawLimitSwitchHit;
    }
    public int getClawPosition()
    {
        return clawPosition;
    }
}