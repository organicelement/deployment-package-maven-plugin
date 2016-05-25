package org.organicelement.deployment.mojo;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;

import org.organicelement.deployment.model.DeploymentPackage;

/**
 * <p>
 * From the Deployment Admin Specification:
 * </p>
 * <p>
 * A Deployment Package consists of installable <i>resources</i>. Resources are
 * described in the <i>Name sections</i> of the Manifest. They are stored in the
 * JAR file under a path. This path is called the <i>resource id</i>. Subsets of
 * these resources are the bundles. Bundles are treated differently from the
 * other resources by the Deployment Admin service. Non-bundle resources are
 * called <i>processed resources</i>.
 * </p>
 */
public class ProcessedResource implements Resource {

    private String filePath;

    private String targetPath;

    private org.organicelement.deployment.model.Resource resource;

    /**
     * A constructor which initializes an instance of a
     * {@link ProcessedResource}.
     */
    public ProcessedResource() {
        filePath = null;
        targetPath = null;

        resource = new org.organicelement.deployment.model.Resource();
    }

    /**
     * @return the file
     */
    public final String getFilePath() {
        return filePath;
    }

    /**
     * @param file the file to set
     */
    public final void setFilePath(final String file) {

        filePath = file;
    }

    /**
     * @return the targetPath
     */
    public final String getTargetPath() {
        return targetPath;
    }

    /**
     * @param target the targetPath to set
     */
    public final void setTargetPath(final String target) {
        targetPath = target;
    }

    /**
     * @return the processor
     */
    public final String getProcessor() {
        return resource.getProcessor();
    }

    /**
     * @param processor the processor to set
     */
    public final void setProcessor(final String processor) {
        resource.setProcessor(processor);
    }

    public void resolve(DeploymentPackage dp, File baseDir) throws MojoExecutionException {
        File f = new File(baseDir, filePath);
        String n = f.getName();

        try {
            resource.setURL(f.toURI().toURL());
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage());
        }
        if (targetPath != null && targetPath.length() > 0) {
            resource.setPath(targetPath + "/" + n);
        } else {
            resource.setPath(n);
        }

        dp.addResource(resource);

    }

    public Object getResource() {
       return resource;
    }


}
