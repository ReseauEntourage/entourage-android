package social.entourage.android.base;

import android.content.Context;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;

import social.entourage.android.BuildConfig;

/**
 * Created by mihaiionescu on 07/07/16.
 */
public class AmazonS3Utils {

    // We only need one instance of the clients and credentials provider
    private static AmazonS3Client sS3Client;
    private static BasicAWSCredentials sCredProvider;

    private static BasicAWSCredentials getCredProvider() {
        if (sCredProvider == null) {
            sCredProvider = new BasicAWSCredentials(BuildConfig.AWS_KEY, BuildConfig.AWS_SECRET);
        }
        return sCredProvider;
    }

    /**
     * Gets an instance of a S3 client which is constructed using the given
     * Context.
     *
     * @return A default S3 client.
     */
    public static AmazonS3Client getS3Client() {
        if (sS3Client == null) {
            sS3Client = new AmazonS3Client(getCredProvider());
            sS3Client.setRegion(Region.getRegion(Regions.EU_WEST_1));
        }
        return sS3Client;
    }

    /**
     * Gets an instance of the TransferUtility which is constructed using the
     * given Context
     *
     * @param context
     * @return a TransferUtility instance
     */
    public static TransferUtility getTransferUtility(Context context) {
        return new TransferUtility(getS3Client(),
                    context.getApplicationContext());
    }

}
