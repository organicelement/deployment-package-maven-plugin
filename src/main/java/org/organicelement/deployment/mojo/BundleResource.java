package org.organicelement.deployment.mojo;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.organicelement.deployment.mojo.util.FileUtil;
import org.apache.maven.plugin.MojoExecutionException;

import org.organicelement.deployment.model.DeploymentPackage;
import org.organicelement.deployment.mojo.util.ManifestBuilder;

/**
 * <p>
 * From the Deployment Admin Specification:
 * </p>
 * <p>
 * A Deployment Package consists of installable <i>resources</i>. Resources are
 * described in the <i>Name sections</i> of the Manifest. They are stored in the
 * JAR file under a path. This path is called the <i>resource id</i>. Subsets of
 * these resources are the bundles. Bundles are treated differently from the
 * other resources by the Deployment Admin service.
 * </p>
 */
public class BundleResource implements Resource {

    private String groupId;

    private String artifactId;

    private String version;
    
    private String classifier;

    private File resolvedFile;

    private String targetPath;

    private org.organicelement.deployment.model.BundleResource bundle;

    private DeploymentPackageMojo mojo;
    
    /**
     * The header entries.
     */
    private List<Header> headerEntries = null;

    /**
     * Constructor which initializes the instance of the {@link BundleResource}.
     */
    public BundleResource() {
        groupId = null;
        artifactId = null;
        version = null;
        targetPath = null;
        mojo = null;
        headerEntries = new ArrayList<Header>();

        bundle = new org.organicelement.deployment.model.BundleResource();
    }

    public final void setMojo(DeploymentPackageMojo mojo) {
        this.mojo = mojo;
    }

    public void resolve(DeploymentPackage dp, File baseDir) throws MojoExecutionException {
        if (resolvedFile == null) {
            resolvedFile = mojo.resolveResource(groupId, artifactId,
                    version, classifier);
            try {
                bundle.setURL(resolvedFile.toURI().toURL());
            } catch (Exception e) {
                throw new MojoExecutionException(
                        "Cannot compute the bundle url : " + e.getMessage());
            }
        }
        
        final List<Header> addedHeaders = getHeaders();
		if ((addedHeaders != null) && (!addedHeaders.isEmpty())) {
			try {
				// modify manifest file to add header values
				JarFile bundleFile = new JarFile(resolvedFile);
				Manifest manifest = bundleFile.getManifest();
				ManifestBuilder mfBuilder = new ManifestBuilder();
				for (Header header : addedHeaders) {
					if (!(header instanceof BundleHeader))
						continue;
					
					mfBuilder.addHeader((BundleHeader) header);
				}

				// modify manifest
				Manifest manipulatedMf = mfBuilder.build(manifest);
				File manipulatedMfFile = File.createTempFile("mf_", ".mf");
				FileOutputStream mfFos = new FileOutputStream(manipulatedMfFile);
				manipulatedMf.write(mfFos);
				mfFos.flush();
				mfFos.close();

				File manipulatedBundleFile = File.createTempFile("bundle_",
						".jar");
				if (manipulatedBundleFile == null)
					throw new MojoExecutionException("Cannot create temp file");

				FileUtil.copyBundleFile(bundleFile, manipulatedBundleFile,
						manipulatedMfFile);

				bundle.setURL(manipulatedBundleFile.toURI().toURL());
			} catch (Exception e) {
				throw new MojoExecutionException(
						"Cannot manipulate manifest file of the original bundle url : " + e.getMessage());
			}
		}

        String resourceId = resolvedFile.getName();
        if (targetPath != null && targetPath.length() > 0) {
            resourceId = targetPath + "/" + resourceId;
        }

        bundle.setPath(resourceId);

        // Add the resource.
        dp.addBundle(bundle);
    }

    /**
     * @return the path and the name of the resource
     * @see Resource#getResourceId()
     */
    public final String getResourceId() {
        return bundle.getName();
    }

    /**
     * @return the targetPath
     */
    public final String getTargetPath() {
        return targetPath;
    }

    /**
     * @param path the targetPath to set
     */
    public final void setTargetPath(final String path) {
        targetPath = path;
    }

    /**
     * @return the customizer
     */
    public final boolean isCustomizer() {
        return bundle.isCustomizer();
    }

    /**
     * @param customizer the customizer to set
     */
    public final void setCustomizer(final boolean customizer) {
        bundle.setCustomizer(customizer);
    }

    public final boolean getMissing() {
        return bundle.isMissing();
    }

    public final void setMissing(final boolean missing) {
        bundle.setMissing(missing);
    }

    /**
     * @return the groupId
     */
    public final String getGroupId() {
        return groupId;
    }

    /**
     * @param group the groupId to set
     */
    public final void setGroupId(final String group) {
        groupId = group;
    }
    
    /**
     * @return the classifier
     */
    public final String getClassifier() {
        return classifier;
    }

    /**
     * @param group the groupId to set
     */
    public final void setClassifier(final String classifier) {
        this.classifier = classifier;
    }

    /**
     * @return the artifactId
     */
    public final String getArtifactId() {
        return artifactId;
    }

    /**
     * @param artifact the artifactId to set
     */
    public final void setArtifactId(final String artifact) {
        artifactId = artifact;
    }

    /**
     * @return the version
     */
    public final String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public final void setVersion(final String version) {
        this.version = version;
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
    public final void setHeaders(final List<Header> headerEntries) {
        // Not processed yet...
        this.headerEntries = headerEntries;
    }

    /**
     * @return the resolvedFile
     * @throws MojoExecutionException
     */
    public final File getResolvedFile() throws MojoExecutionException {
        if (resolvedFile == null) {
        	//System.out.println("MOJO : " + mojo);
        	System.out.println("Artifact : " + groupId + ":" + artifactId + ":" + version);

            resolvedFile = mojo.resolveResource(groupId, artifactId,
                    version, classifier);
            try {
                bundle.setURL(resolvedFile.toURI().toURL());
            } catch (Exception e) {
                throw new MojoExecutionException(e.getMessage());
            }
        }
        return resolvedFile;
    }

    public Object getResource() {
        return bundle;
    }

    /**
     * @see java.lang.Object#toString()
     * @return a string representation of a {@link BundleResource}.
     */
    public final String toString() {

        String rf = null;
        try {
            rf = getResolvedFile().getAbsolutePath();
        } catch (Throwable e) {
           // Silently ignore the exception.
        }

        final StringBuffer buffer = new StringBuffer(this.getClass().getName());
        buffer.append("[");
        buffer.append("groupId:");
        buffer.append(getGroupId());
        buffer.append(", ");
        buffer.append("artifactId:");
        buffer.append(getArtifactId());
        buffer.append(", ");
        buffer.append("version:");
        buffer.append(getVersion());
        buffer.append(", ");
        buffer.append("resolvedFile:");
        buffer.append(rf);
        buffer.append("]");
        return buffer.toString();
    }


}
