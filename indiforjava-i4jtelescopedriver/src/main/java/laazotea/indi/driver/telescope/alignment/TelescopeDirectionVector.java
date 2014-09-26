package laazotea.indi.driver.telescope.alignment;

import org.gnu.savannah.gsl.CBLAS_TRANSPOSE;
import org.gnu.savannah.gsl.Gsl;
import org.gnu.savannah.gsl.GslMatrix;
import org.gnu.savannah.gsl.GslVector;

public class TelescopeDirectionVector implements Cloneable {

    private double x;

    private double y;

    private double z;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /** Default constructor */

    public TelescopeDirectionVector() {
        x = 0;
        y = 0;
        z = 0;
    }

    /** Copy constructor */
    public TelescopeDirectionVector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public TelescopeDirectionVector multiply(TelescopeDirectionVector rhs) {
        TelescopeDirectionVector result = new TelescopeDirectionVector();

        result.x = y * rhs.z - z * rhs.y;
        result.y = z * rhs.x - x * rhs.z;
        result.z = x * rhs.y - y * rhs.x;
        return result;

    }

    public TelescopeDirectionVector multiply(double rhs) {
        TelescopeDirectionVector result = new TelescopeDirectionVector();

        result.x = x * rhs;
        result.y = y * rhs;
        result.z = z * rhs;
        return result;

    }

    public TelescopeDirectionVector multiplyAsign(double rhs) {
        x = x * rhs;
        y = y * rhs;
        z = z * rhs;
        return this;

    }

    public TelescopeDirectionVector minus(TelescopeDirectionVector rhs) {
        return new TelescopeDirectionVector(x - rhs.x, y - rhs.y, z - rhs.z);

    }

    public double dotProduct(TelescopeDirectionVector rhs) {
        return x * rhs.x + y * rhs.y + z * rhs.z;

    }

    public double length() {
        return Math.sqrt(x * x + y * y + z * z);

    }

    public void normalise() {
        double length = Math.sqrt(x * x + y * y + z * z);
        x /= length;
        y /= length;
        z /= length;

    }

    /**
     * Rotate the reference frame around the Y axis. This has the affect of
     * rotating the vector itself in the opposite direction
     * 
     * @param Angle
     *            The angle to rotate the reference frame by. Positive values
     *            give an anti-clockwise rotation from the perspective of
     *            looking down the positive axis towards the origin.
     **/
    void RotateAroundY(double Angle){
        Angle = Angle * Math.PI / 180.0;
        GslVector pGSLInputVector = new GslVector(3);
        pGSLInputVector.set( 0, x);
        pGSLInputVector.set( 1, y);
        pGSLInputVector.set( 2, z);
        GslMatrix pRotationMatrix = new GslMatrix(3, 3);
        pRotationMatrix.set( 0, 0, Math.cos(Angle));
        pRotationMatrix.set( 0, 1, 0.0);
        pRotationMatrix.set( 0, 2, Math.sin(Angle));
        pRotationMatrix.set( 1, 0, 0.0);
        pRotationMatrix.set( 1, 1, 1.0);
        pRotationMatrix.set( 1, 2, 0.0);
        pRotationMatrix.set( 2, 0, -Math.sin(Angle));
        pRotationMatrix.set( 2, 1, 0.0);
        pRotationMatrix.set( 2, 2, Math.cos(Angle));
        GslVector pGSLOutputVector = new GslVector(3);
        pGSLOutputVector.setZero();
        Gsl.gsl_blas_dgemv(CBLAS_TRANSPOSE.CblasNoTrans, 1.0, pRotationMatrix, pGSLInputVector, 0.0, pGSLOutputVector);
        x = pGSLOutputVector.get( 0);
        y = pGSLOutputVector.get( 1);
        z = pGSLOutputVector.get( 2);
    }
}
