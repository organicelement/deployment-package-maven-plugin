package org.organicelement.deployment.model;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static org.junit.Assert.*;


public class APITest {

    public static final String BUNDLE1_SN = "org.apache.felix.configadmin";
    public static final String BUNDLE1_V = "1.2.4";
    public static final String BUNDLE2_SN = "org.apache.felix.shell";
    public static final String BUNDLE2_V = "1.4.0";

    public static final String RESOURCE_PROCESSOR = "org.osgi.deployment.rp.autoconf";
    public static final String RESOURCE_NAME = "pax-web.xml";


    public static URL BUNDLE1;
    public static URL BUNDLE2;
    public static URL RESOURCE;

    static {
        try {
            BUNDLE1 = new URL("file:src/test/resources/bundles/org.apache.felix.configadmin-1.2.4.jar");
            BUNDLE2 = new URL("file:src/test/resources/bundles/org.apache.felix.shell-1.4.0.jar");
            RESOURCE = new URL("file:src/test/resources/conf/pax-web.xml");
        } catch (MalformedURLException e) {
            fail("Cannot create bundle url");
        }
    }

    @Test
    public void testCreationWithOneBundle() throws IOException, CheckingException {
        DeploymentPackage dp = new DeploymentPackage();

        dp.addBundle(BUNDLE1).setSymbolicName("my.first.dp").setVersion("1.0.0");

        File dpf = new File("target/dp-test/dp1.dp");

        dp.build(dpf);

        JarFile jar = new JarFile(dpf);
        Manifest man = jar.getManifest();

        System.out.println(man.getMainAttributes().entrySet());

        String sn = man.getMainAttributes().getValue(Constants.DEPLOYMENTPACKAGE_SYMBOLICMAME);
        String v = man.getMainAttributes().getValue(Constants.DEPLOYMENTPACKAGE_VERSION);


        assertTrue("my.first.dp".equals(sn));
        assertTrue("1.0.0".equals(v));

        Attributes att = man.getEntries().get(BUNDLE1_SN + ".jar"); // Generated path
        assertNotNull(att);

        String bsn = att.getValue(Constants.BUNDLE_SYMBOLICNAME);
        String bv = att.getValue(Constants.BUNDLE_VERSION);
        String bsha = att.getValue(Constants.SHA_ATTRIBUTE);

        assertTrue(BUNDLE1_SN.equals(bsn));
        assertTrue(BUNDLE1_V.equals(bv));
        assertNotNull(bsha);

        JarEntry je = jar.getJarEntry(BUNDLE1_SN + ".jar");
        assertNotNull(je);
    }

    @Test
    public void testCreationWithOneBundleWithAPath() throws IOException, CheckingException {
        DeploymentPackage dp = new DeploymentPackage();

        dp.addBundle(BUNDLE1, "bundle/b1.jar").setSymbolicName("my.first2.dp").setVersion("1.0.0");

        File dpf = new File("target/dp-test/dp1-1.dp");

        dp.build(dpf);

        JarFile jar = new JarFile(dpf);
        Manifest man = jar.getManifest();

        System.out.println(man.getMainAttributes().entrySet());

        String sn = man.getMainAttributes().getValue(Constants.DEPLOYMENTPACKAGE_SYMBOLICMAME);
        String v = man.getMainAttributes().getValue(Constants.DEPLOYMENTPACKAGE_VERSION);


        assertTrue("my.first2.dp".equals(sn));
        assertTrue("1.0.0".equals(v));

        Attributes att = man.getEntries().get("bundle/b1.jar"); // Generated path
        assertNotNull(att);

        String bsn = att.getValue(Constants.BUNDLE_SYMBOLICNAME);
        String bv = att.getValue(Constants.BUNDLE_VERSION);
        String bsha = att.getValue(Constants.SHA_ATTRIBUTE);

        assertTrue(BUNDLE1_SN.equals(bsn));
        assertTrue(BUNDLE1_V.equals(bv));
        assertNotNull(bsha);

        JarEntry je = jar.getJarEntry("bundle/b1.jar");
        assertNotNull(je);
    }

    @Test
    public void testCreationWithTwoBundles() throws IOException, CheckingException {
        DeploymentPackage dp = new DeploymentPackage();

        dp.addBundle(BUNDLE1).addBundle(BUNDLE2)
            .setSymbolicName("my.second.dp").setVersion("1.0.0");

        File dpf = new File("target/dp-test/dp2.dp");

        dp.build(dpf);

        JarFile jar = new JarFile(dpf);
        Manifest man = jar.getManifest();

        System.out.println(man.getMainAttributes().entrySet());

        String sn = man.getMainAttributes().getValue(Constants.DEPLOYMENTPACKAGE_SYMBOLICMAME);
        String v = man.getMainAttributes().getValue(Constants.DEPLOYMENTPACKAGE_VERSION);

        assertTrue("my.second.dp".equals(sn));
        assertTrue("1.0.0".equals(v));

        Attributes att = man.getEntries().get(BUNDLE1_SN + ".jar"); // Generated path
        assertNotNull(att);

        String bsn = att.getValue(Constants.BUNDLE_SYMBOLICNAME);
        String bv = att.getValue(Constants.BUNDLE_VERSION);
        String bsha = att.getValue(Constants.SHA_ATTRIBUTE);

        assertTrue(BUNDLE1_SN.equals(bsn));
        assertTrue(BUNDLE1_V.equals(bv));
        assertNotNull(bsha);

        att = man.getEntries().get(BUNDLE2_SN + ".jar"); // Generated path
        assertNotNull(att);

        bsn = att.getValue(Constants.BUNDLE_SYMBOLICNAME);
        bv = att.getValue(Constants.BUNDLE_VERSION);
        bsha = att.getValue(Constants.SHA_ATTRIBUTE);

        assertTrue(BUNDLE2_SN.equals(bsn));
        assertTrue(BUNDLE2_V.equals(bv));
        assertNotNull(bsha);

        JarEntry je = jar.getJarEntry(BUNDLE1_SN + ".jar");
        assertNotNull(je);

        je = jar.getJarEntry(BUNDLE2_SN + ".jar");
        assertNotNull(je);

    }

    @Test
    public void testCreationWithOneBundleAndAResource() throws IOException, CheckingException {
        DeploymentPackage dp = new DeploymentPackage();

        dp
            .addResource(RESOURCE, RESOURCE_PROCESSOR)
            .addBundle(BUNDLE1)
            .setSymbolicName("my.third.dp").setVersion("1.0.0");

        File dpf = new File("target/dp-test/dp3.dp");

        dp.build(dpf);

        JarFile jar = new JarFile(dpf);
        Manifest man = jar.getManifest();

        String sn = man.getMainAttributes().getValue(Constants.DEPLOYMENTPACKAGE_SYMBOLICMAME);
        String v = man.getMainAttributes().getValue(Constants.DEPLOYMENTPACKAGE_VERSION);


        assertTrue("my.third.dp".equals(sn));
        assertTrue("1.0.0".equals(v));

        Attributes att = man.getEntries().get(BUNDLE1_SN + ".jar"); // Generated path
        assertNotNull(att);

        String bsn = att.getValue(Constants.BUNDLE_SYMBOLICNAME);
        String bv = att.getValue(Constants.BUNDLE_VERSION);
        String bsha = att.getValue(Constants.SHA_ATTRIBUTE);

        assertTrue(BUNDLE1_SN.equals(bsn));
        assertTrue(BUNDLE1_V.equals(bv));
        assertNotNull(bsha);

        JarEntry je = jar.getJarEntry(BUNDLE1_SN + ".jar");
        assertNotNull(je);

        att = man.getEntries().get(RESOURCE_NAME);
        assertNotNull(att);
        String rp = att.getValue(Constants.RESOURCE_PROCESSOR);

        assertTrue(RESOURCE_PROCESSOR.equals(rp));

        JarEntry je2 = jar.getJarEntry("pax-web.xml"); // Computed.
        assertNotNull(je2);

    }

    @Test
    public void testCreationWithTwoBundlesAndAResource() throws IOException, CheckingException {
        DeploymentPackage dp = new DeploymentPackage();

        dp.addBundle(BUNDLE1)
            .addResource(RESOURCE, RESOURCE_PROCESSOR, "conf/pax.xml")
            .addBundle(BUNDLE2)
            .setSymbolicName("my.fourth.dp").setVersion("1.0.0");

        File dpf = new File("target/dp-test/dp4.dp");

        dp.build(dpf);

        JarFile jar = new JarFile(dpf);
        Manifest man = jar.getManifest();

        System.out.println(man.getMainAttributes().entrySet());

        String sn = man.getMainAttributes().getValue(Constants.DEPLOYMENTPACKAGE_SYMBOLICMAME);
        String v = man.getMainAttributes().getValue(Constants.DEPLOYMENTPACKAGE_VERSION);

        assertTrue("my.fourth.dp".equals(sn));
        assertTrue("1.0.0".equals(v));

        Attributes att = man.getEntries().get(BUNDLE1_SN + ".jar"); // Generated path
        assertNotNull(att);

        String bsn = att.getValue(Constants.BUNDLE_SYMBOLICNAME);
        String bv = att.getValue(Constants.BUNDLE_VERSION);
        String bsha = att.getValue(Constants.SHA_ATTRIBUTE);

        assertTrue(BUNDLE1_SN.equals(bsn));
        assertTrue(BUNDLE1_V.equals(bv));
        assertNotNull(bsha);

        att = man.getEntries().get(BUNDLE2_SN + ".jar"); // Generated path
        assertNotNull(att);

        bsn = att.getValue(Constants.BUNDLE_SYMBOLICNAME);
        bv = att.getValue(Constants.BUNDLE_VERSION);
        bsha = att.getValue(Constants.SHA_ATTRIBUTE);

        assertTrue(BUNDLE2_SN.equals(bsn));
        assertTrue(BUNDLE2_V.equals(bv));
        assertNotNull(bsha);

        JarEntry je = jar.getJarEntry(BUNDLE1_SN + ".jar");
        assertNotNull(je);

        je = jar.getJarEntry(BUNDLE2_SN + ".jar");
        assertNotNull(je);

        att = man.getEntries().get("conf/pax.xml");
        assertNotNull(att);
        String rp = att.getValue(Constants.RESOURCE_PROCESSOR);

        assertTrue(RESOURCE_PROCESSOR.equals(rp));

        JarEntry je2 = jar.getJarEntry("conf/pax.xml"); // Set
        assertNotNull(je2);
    }

    @Test
    public void testWithACustomizer() throws IOException, CheckingException {
        DeploymentPackage dp = new DeploymentPackage();

        dp
            .addBundle(BUNDLE1)
            .addBundle(new BundleResource().setURL(BUNDLE2).setCustomizer(true))
            .setSymbolicName("my.fifth.dp").setVersion("1.0.0");

        File dpf = new File("target/dp-test/dp5.dp");

        dp.build(dpf);

        JarFile jar = new JarFile(dpf);
        Manifest man = jar.getManifest();

        System.out.println(man.getMainAttributes().entrySet());

        String sn = man.getMainAttributes().getValue(Constants.DEPLOYMENTPACKAGE_SYMBOLICMAME);
        String v = man.getMainAttributes().getValue(Constants.DEPLOYMENTPACKAGE_VERSION);

        assertTrue("my.fifth.dp".equals(sn));
        assertTrue("1.0.0".equals(v));

        Attributes att = man.getEntries().get(BUNDLE1_SN + ".jar"); // Generated path
        assertNotNull(att);

        String bsn = att.getValue(Constants.BUNDLE_SYMBOLICNAME);
        String bv = att.getValue(Constants.BUNDLE_VERSION);
        String bsha = att.getValue(Constants.SHA_ATTRIBUTE);

        assertTrue(BUNDLE1_SN.equals(bsn));
        assertTrue(BUNDLE1_V.equals(bv));
        assertNotNull(bsha);

        att = man.getEntries().get(BUNDLE2_SN + ".jar"); // Generated path
        assertNotNull(att);

        bsn = att.getValue(Constants.BUNDLE_SYMBOLICNAME);
        bv = att.getValue(Constants.BUNDLE_VERSION);
        bsha = att.getValue(Constants.SHA_ATTRIBUTE);
        String bcutomizer = att.getValue(Constants.DEPLOYMENTPACKAGE_CUSTOMIZER);


        assertTrue(BUNDLE2_SN.equals(bsn));
        assertTrue(BUNDLE2_V.equals(bv));
        assertTrue("true".equals(bcutomizer));

        assertNotNull(bsha);

        JarEntry je = jar.getJarEntry(BUNDLE1_SN + ".jar");
        assertNotNull(je);

        je = jar.getJarEntry(BUNDLE2_SN + ".jar");
        assertNotNull(je);
    }

    @Test
    public void testFixPack() throws IOException, CheckingException {
        DeploymentPackage dp = new DeploymentPackage();

        dp
            .addBundle(BUNDLE1)
            .addBundle(new BundleResource().setURL(BUNDLE2).setMissing(true))
            .setSymbolicName("my.fifth.dp").setVersion("1.0.0")
            .setFixPackage("[1.0.0, 2.0.0)"); // Version Range

        File dpf = new File("target/dp-test/dp5-fix.dp");

        dp.build(dpf);

        JarFile jar = new JarFile(dpf);
        Manifest man = jar.getManifest();

        String sn = man.getMainAttributes().getValue(Constants.DEPLOYMENTPACKAGE_SYMBOLICMAME);
        String v = man.getMainAttributes().getValue(Constants.DEPLOYMENTPACKAGE_VERSION);
        String fp = man.getMainAttributes().getValue(Constants.DEPLOYMENTPACKAGE_FIXPACK);

        assertTrue("my.fifth.dp".equals(sn));
        assertTrue("1.0.0".equals(v));
        assertTrue("[1.0.0, 2.0.0)".equals(fp));

        Attributes att = man.getEntries().get(BUNDLE1_SN + ".jar"); // Generated path
        assertNotNull(att);

        String bsn = att.getValue(Constants.BUNDLE_SYMBOLICNAME);
        String bv = att.getValue(Constants.BUNDLE_VERSION);
        String bsha = att.getValue(Constants.SHA_ATTRIBUTE);

        assertTrue(BUNDLE1_SN.equals(bsn));
        assertTrue(BUNDLE1_V.equals(bv));
        assertNotNull(bsha);

        att = man.getEntries().get(BUNDLE2_SN + ".jar"); // Generated path
        assertNotNull(att);

        bsn = att.getValue(Constants.BUNDLE_SYMBOLICNAME);
        bv = att.getValue(Constants.BUNDLE_VERSION);
        bsha = att.getValue(Constants.SHA_ATTRIBUTE);
        String bmissing = att.getValue(Constants.DEPLOYMENTPACKAGE_MISSING);


        assertTrue(BUNDLE2_SN.equals(bsn));
        assertTrue(BUNDLE2_V.equals(bv));
        assertTrue("true".equals(bmissing));

        assertNotNull(bsha);

        JarEntry je = jar.getJarEntry(BUNDLE1_SN + ".jar");
        assertNotNull(je);

        je = jar.getJarEntry(BUNDLE2_SN + ".jar");
        assertNull(je); // Missing
    }

    @Test
    public void testEntries() throws IOException, CheckingException {
        DeploymentPackage dp = new DeploymentPackage();
        File dpf = new File("target/dp-test/dp6.dp");

        dp.addBundle(BUNDLE1)
            .setSymbolicName("my.other.dp")
            .setVersion("1.0.0")
            .setContactAddress("foo@bar.com")
            .setCopyright("FooCorp")
            .setDescription("This is a cool DP")
            .setDocURL(new URL("http://perdu.com"))
            .setIcon(new URL("file:myicon.png"))
            .setLicense("ASL 2.0")
            .setName("My DP")
            .setVendor("Foo Corp")
            .addManifestEntry("built-by", "me")
            .build(dpf);

        JarFile jar = new JarFile(dpf);
        Manifest man = jar.getManifest();

        String sn = man.getMainAttributes().getValue(Constants.DEPLOYMENTPACKAGE_SYMBOLICMAME);
        String v = man.getMainAttributes().getValue(Constants.DEPLOYMENTPACKAGE_VERSION);
        String fp = man.getMainAttributes().getValue(Constants.DEPLOYMENTPACKAGE_FIXPACK);
        String ca = man.getMainAttributes().getValue(Constants.DEPLOYMENTPACKAGE_CONTACTADDRESS);
        String co = man.getMainAttributes().getValue(Constants.DEPLOYMENTPACKAGE_COPYRIGHT);
        String de = man.getMainAttributes().getValue(Constants.DEPLOYMENTPACKAGE_DESCRIPTION);
        String doc = man.getMainAttributes().getValue(Constants.DEPLOYMENTPACKAGE_DOCURL);
        String ic = man.getMainAttributes().getValue(Constants.DEPLOYMENTPACKAGE_ICON);
        String li = man.getMainAttributes().getValue(Constants.DEPLOYMENTPACKAGE_LICENSE);
        String na = man.getMainAttributes().getValue(Constants.DEPLOYMENTPACKAGE_NAME);
        String ve = man.getMainAttributes().getValue(Constants.DEPLOYMENTPACKAGE_VENDOR);

        String bb = man.getMainAttributes().getValue("built-by");


        assertEquals("my.other.dp", sn);
        assertEquals("1.0.0", v);
        assertNull(fp);
        assertEquals("foo@bar.com", ca);
        assertEquals("FooCorp", co);
        assertEquals("This is a cool DP", de);
        assertEquals("http://perdu.com", doc);
        assertEquals("file:myicon.png", ic);
        assertEquals("ASL 2.0", li);
        assertEquals("My DP", na);
        assertEquals("Foo Corp", ve);
        assertEquals("me", bb);


    }

    @Test(expected=CheckingException.class)
    public void testMissingSymbolicName() throws CheckingException, IOException {
        new DeploymentPackage().build();
    }

    @Test(expected=CheckingException.class)
    public void testMissingVersion() throws CheckingException, IOException {
        new DeploymentPackage().setSymbolicName("sn").build();
    }






}
