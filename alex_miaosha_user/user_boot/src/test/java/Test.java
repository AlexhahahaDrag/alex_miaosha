import org.jasypt.util.text.BasicTextEncryptor;

/**
 * description:
 * author:       majf
 * createDate:   2022/12/29 9:50
 * version:      1.0.0
 */
public class Test {

    public static void main(String[] args) {
        //该类的选择根据algorithm：PBEWithMD5AndDE选择的算法选择
        BasicTextEncryptor encryptor = new BasicTextEncryptor();
        encryptor.setPassword("02700083-9fd9-4b82-a4b4-9177e0560e92");
//        String encrypt = encryptor.encrypt("nacos");
//        System.out.println(encrypt);
//        String decrypt = encryptor.decrypt(encrypt);
//        System.out.println(decrypt);
//        String encrypt1 = encryptor.encrypt("nacos");
//        System.out.println(encrypt1);
//        String decrypt1 = encryptor.decrypt(encrypt1);
//        System.out.println(decrypt1);
        System.out.println(encryptor.decrypt("TQ2oVKN42O4FWPbyKH7mCHBwhNc4xNhLZa2IBDN93TI="));
    }
}
