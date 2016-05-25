package org.organicelement.deployment.mojo;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.organicelement.deployment.model.DeploymentPackage;

/**
 * <p>
 * This class represents a Deployment-Package and stores some information about
 * it.
 * </p>
 * <p>
 * The resources if a Deployment-Package are categorized in three groups:
 * <ul>
 * <li>Bundle-Resources</li>
 * <li>Localization-Resources (not supported)</li>
 * <li>Processed-Resources</li>
 * </ul>
 * </p>
 */
public class DeploymentPackageMetadata {

    private DeploymentPackage deploymentPackage;

    /**
     * The resources.
     */
    private List<Resource> resources = null;
    
    /**
     * The header entries.
     */
    private List<Header> headerEntries = null;

    /**
     * A constructor which initializes an instance of a
     * {@link DeploymentPackageMetadata}.
     */
    public DeploymentPackageMetadata() {
        resources = new ArrayList<Resource>();
        deploymentPackage = new DeploymentPackage();
        headerEntries = new ArrayList<Header>();
    }

    /**
     * @return the description
     */
    public final String getDescription() {
        return deploymentPackage.getDescription();
    }

    /**
     * @param desc the description to set
     */
    public final void setDescription(final String desc) {
        deploymentPackage.setDescription(desc);
    }

    /**
     * @return the name
     */
    public final String getName() {
        return deploymentPackage.getName();
    }

    /**
     * @param name the name to set
     */
    public final void setName(final String name) {
        deploymentPackage.setName(name);
    }

    /**
     * @return the version
     */
    public final String getVersion() {
        return deploymentPackage.getVersion();
    }

    /**
     * @param version the version to set
     */
    public final void setVersion(final String version) {
        deploymentPackage.setVersion(version);
    }

    /**
     * @return the required space
     */
    public final long getRequiredStorage() {
        return deploymentPackage.getRequiredStorage();
    }

    /**
     * @param desc the description to set
     */
    public final void setRequiredStorage(final long space) {
        deploymentPackage.setRequiredStorage(space);
    }

    /**
     * @return the license
     */
    public final String getLicense() {
        return deploymentPackage.getLicense();
    }

    /**
     * @param license the license to set
     */
    public final void setLicense(final String license) {
        deploymentPackage.setLicense(license);
    }

    /**
     * @return the copyright
     */
    public final String getCopyright() {
        return deploymentPackage.getCopyright();
    }

    /**
     * @param cr the copyright to set
     */
    public final void setCopyright(final String cr) {
        deploymentPackage.setCopyright(cr);
    }

    /**
     * @return the contactAddress
     */
    public final String getContactAddress() {
        return deploymentPackage.getAddress();
    }

    /**
     * @param ad the contactAddress to set
     */
    public final void setContactAddress(final String ad) {
        deploymentPackage.setContactAddress(ad);
    }

    /**
     * @return the docURL
     */
    public final String getDocURL() {
        if (deploymentPackage.getDocURL() != null) {
            return deploymentPackage.getDocURL().toExternalForm();
        } else {
            return null;
        }
    }

    /**
     * @param url the docURL to set
     * @throws MalformedURLException
     */
    public final void setDocURL(final String url) throws MalformedURLException {
        deploymentPackage.setDocURL(new URL(url));
    }

    /**
     * @param url the icon url to set
     * @throws MalformedURLException
     */
    public final void setIcon(final String url) throws MalformedURLException {
        deploymentPackage.setIcon(new URL(url));
    }

    /**
     * @return the icon
     */
    public final String getIcon() {
        if (deploymentPackage.getIcon() != null) {
            return deploymentPackage.getIcon().toExternalForm();
        } else {
            return null;
        }
    }


    /**
     * @return the vendor
     */
    public final String getVendor() {
        return deploymentPackage.getVendor();
    }

    /**
     * @param vendor the vendor to set
     */
    public final void setVendor(final String vendor) {
        deploymentPackage.setVendor(vendor);
    }

    /**
     * @return the fixPack
     */
    public final String getFixPack() {
        return deploymentPackage.getFixPack();
    }

    /**
     * @param fixPack the fixPack to set
     */
    public final void setFixPack(final String fixPack) {
        deploymentPackage.setFixPackage(fixPack);
    }

    /**
     * @return the resources
     */
    public final List<Resource> getResources() {
        return resources;
    }
    
    /**
     * @return the header entries
     */
    public final List<Header> getHeaders() {
        return headerEntries;
    }

    /**
     * @param resources the resources to set
     */
    public final void setResources(final List<Resource> resources) {
        // Not processed yet...
        this.resources = resources;
    }
    
    /**
     * @param resources the resources to set
     */
    public final void setHeaders(final List<Header> headerEntries) {
        // Not processed yet...
        this.headerEntries = headerEntries;
    }

    /**
     * @return the symbolicName
     */
    public final String getSymbolicName() {
        return deploymentPackage.getSymbolicName();
    }

    /**
     * @param sn the symbolicName to set
     */
    public final void setSymbolicName(final String sn) {
        // Compute singleton:=
        String symb = sn.trim();
        int index = symb.indexOf(";singleton:=");
        if (index != -1) {
            deploymentPackage.setSymbolicName(symb.substring(0, index));
        } else {
            deploymentPackage.setSymbolicName(symb);
        }
    }

    /**
     * Resources can be Bundle-, or Processed-Resources. This method filters the
     * whole resource list and returns only Bundle-Resources.
     * @return A filtered resource list where only Bundle-Resources are
     *         contained.
     */
    public final List<BundleResource> getBundleResources() {
        final List<BundleResource> list = new ArrayList<BundleResource>();
        for (final Resource resource : resources) {
            if (resource instanceof BundleResource) {
                list.add((BundleResource) resource);
            }
        }
        return list;
    }

    /**
     * Resources can be Bundle-, or Processed-Resources. This method filters the
     * whole resource list and returns only Processed-Resources.
     * @return A filtered resource list where only Processed-Resources are
     *         contained.
     */
    public final List<ProcessedResource> getProcessedResources() {
        final List<ProcessedResource> list = new ArrayList<ProcessedResource>();
        for (final Resource resource : resources) {
            if (resource instanceof ProcessedResource) {
                list.add((ProcessedResource) resource);
            }
        }

        return list;
    }

    public DeploymentPackage getDeploymentPackage() {
        return deploymentPackage;
    }
}
