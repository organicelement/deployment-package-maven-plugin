package org.organicelement.deployment.mojo;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.License;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.organicelement.deployment.model.CheckingException;
import org.organicelement.deployment.model.DeploymentPackage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * Create an OSGi deployment package from Maven project.
 * 
 */
@Mojo( name = "package", requiresDependencyResolution = ResolutionScope.RUNTIME,
		threadSafe = true,
		defaultPhase = LifecyclePhase.PACKAGE)
public class DeploymentPackageMojo extends AbstractMojo {

	private static final String DP_FILE_EXTENSION = ".dp";

	private static final String PLUGIN_NAME = "org.organicelement:deployment-package-maven-plugin - 1.0.0-SNAPSHOT";

	/**
	 * The directory for the generated bundles.
	 * 
	 */
	@Parameter(property = "project.build.outputDirectory", required = true)
	private File ouputDirectory;

	/**
	 * The directory for the pom.
	 * 
	 */
	@Parameter(property = "basedir", required = true)
	private File basedir;

	/**
	 * The infos for the deployment-package.
	 * 
	 */
	@Parameter(property="deploymentPackage", required = true)
	private DeploymentPackageMetadata deploymentPackageInfo = null;

	/**
	 * Directory where the manifest will be written.
	 * 
	 */
	@Parameter(property="manifestLocation", defaultValue="${project.build.outputDirectory}/META-INF")
	private File manifestLocation;

	/**
	 * The Maven project.
	 * 
	 */
	@Parameter(property = "project", required = true, readonly = true)
	private MavenProject project;

	/**
	 * The directory for the generated JAR.
	 * 
	 */
	@Parameter(property = "project.build.directory", required = true)
	private String buildDirectory;

	/**
	 * Project types which this plugin supports.
	 * 
	 */
	@Parameter(alias = "supportedProjectTypes")
	private List<String> supportedProjectTypes = Arrays.asList(new String[] { "deployment-package" });

	/**
	 * The local repository used to resolve artifacts.
	 * 
	 */
	@Parameter(property = "localRepository", required = true)
	private ArtifactRepository localRepository;

	/**
	 * The remote repositories used to resolve artifacts.
	 * 
	 */
	@Parameter(property="remoteRepositories", required = true)
	private List<ArtifactRepository> remoteRepositories;

	/**
	 * Flag that indicates if the manifest of the resulting deployment-package should contain extra data like
	 * "Created-By, Creation-Date, ...".
	 * 
	 */
	@Parameter(property = "writeExtraData")
	private boolean writeExtraData = true;

	/**
	 * Flag that indicates if the resulting deployment-package should contain the project dependencies. Only bundles are
	 * considered.
	 * 
	 */
	@Parameter(property="includeDependencies")
	private boolean includeDependencies = true;

	@Component
	private ArtifactFactory artifactFactory;

	@Component
	private ArtifactResolver artifactresolver;

	@Component
	private ArtifactHandlerManager artifactHandlerManager;

	@Component
	private ArchiverManager archiverManager;

	/**
	 * This method will be called by the Maven framework in order to execute this plugin.
	 * 
	 * @throws MojoExecutionException id any error occures
	 * @throws MojoFailureException id any error occures
	 */
	public final void execute() throws MojoExecutionException, MojoFailureException {

		// First, check if we support such type of packaging.
		final Artifact artifact = getProject().getArtifact();
		if (!getSupportedProjectTypes().contains(artifact.getType())) {
			getLogger().debug(
			      "Ignoring project " + artifact + " : type " + artifact.getType()
			            + " is not supported by bundle plugin, supported types are " + getSupportedProjectTypes());
			return;
		}

		// Create a description from the pom metadata.
		DeploymentPackageMetadata deploymentPackageInfo = getDeploymentPackageInfo();
		if (deploymentPackageInfo == null) {
			throw new MojoExecutionException("No deployment package described");
		}

		// add project dependencies
		if (includeDependencies)
			addDependencies(deploymentPackageInfo);

		// add inherited headers
		addHeaders(deploymentPackageInfo);

		DeploymentPackage currentPackage = deploymentPackageInfo.getDeploymentPackage();

		// Populate...
		try {
			populate(currentPackage, deploymentPackageInfo);
		} catch (IOException e1) {
			throw new MojoExecutionException("Cannot analyze the artifact : " + e1.getMessage());
		}

		// Resolve all resources.
		for (BundleResource br : deploymentPackageInfo.getBundleResources()) {
			br.setMojo(this);
			br.resolve(currentPackage, getBaseDir());
		}

		for (ProcessedResource pr : deploymentPackageInfo.getProcessedResources()) {
			pr.resolve(currentPackage, getBaseDir());
		}

		// Check...
		try {
			currentPackage.check();
		} catch (CheckingException e) {
			throw new MojoExecutionException("The deployment package is inconsistent : " + e.getMessage());
		}

		// Now, handle file creation...
		String finalName = getProject().getBuild().getFinalName() + DP_FILE_EXTENSION;
		final File file = new File(getBuildDirectory(), finalName);

		file.getParentFile().mkdirs();

		// // workaround for MNG-1682: force maven to install artifact using the
		// // "jar" handler
		final Artifact mainArtifact = getProject().getArtifact();
		// mainArtifact.setArtifactHandler(getArtifactHandlerManager()
		// .getArtifactHandler("jar"));
		mainArtifact.setFile(file);

		// Build...
		try {
			getLogger().debug("Build the deployment package");
			currentPackage.build(file);
			getLogger().debug("Deployment package built");
		} catch (Exception e) {
			throw new MojoExecutionException("The deployment package cannot be built : " + e.getMessage());
		}

	}

	private void addHeaders(DeploymentPackageMetadata dpInfo) {
		List<Header> headers = dpInfo.getHeaders();
		for (BundleResource bres : dpInfo.getBundleResources()) {
			for (Header header : headers) {
				bres.getHeaders().add(header.clone());
			}
		}
	}

	/**
	 * Plugin configuration have been performed before calling this method
	 * 
	 * @param dpInfo current deployment package configuration
	 */
	private void addDependencies(DeploymentPackageMetadata dpInfo) {
		List dependencies = project.getDependencies();
		if (dependencies != null) {
			for (Object depObj : dependencies) {
				if (!(depObj instanceof Dependency))
					continue;

				Dependency dep = (Dependency) depObj;
				if (!dep.getType().equals("jar")) // TODO should detect if it is not a bundle
					continue;

				String groupId = dep.getGroupId();
				String artifactId = dep.getArtifactId();
				String version = dep.getVersion();

				BundleResource bres = getDefinedDPResource(groupId, artifactId, dpInfo);
				if (bres == null) {
					bres = new BundleResource();
					dpInfo.getResources().add(bres);
				}

				bres.setGroupId(groupId);
				bres.setArtifactId(artifactId);
				bres.setVersion(version);
				bres.setTargetPath("bundles");
			}
		}
	}

	private BundleResource getDefinedDPResource(String groupId, String artifactId, DeploymentPackageMetadata dpInfo) {
		for (BundleResource bres : dpInfo.getBundleResources()) {
			if (bres.getGroupId().equals(groupId) && bres.getArtifactId().equals(artifactId)) {
				return bres;
			}
		}

		return null;
	}

	private void populate(DeploymentPackage currentPackage, DeploymentPackageMetadata dpInfo) throws IOException {
		Maven2OsgiConverter converter = new Maven2OsgiConverter();
		if (currentPackage.getSymbolicName() == null) {
			currentPackage.setSymbolicName(converter.getBundleSymbolicName(getProject().getArtifact()));
		}

		if (currentPackage.getVersion() == null) {
			currentPackage.setVersion(converter.getVersion(getProject().getArtifact()));
		}

		if (currentPackage.getAddress() == null) {
			if (getProject().getOrganization() != null) {
				currentPackage.setContactAddress(getProject().getOrganization().getUrl());
			}
		}

		if (currentPackage.getCopyright() == null) {
			if (getProject().getOrganization() != null) {
				currentPackage.setCopyright(getProject().getOrganization().getName());
			}
		}

		if (currentPackage.getDescription() == null) {
			if (getProject().getDescription() != null) {
				currentPackage.setDescription(getProject().getDescription());
			}
		}

		if (currentPackage.getDocURL() == null) {
			if (getProject().getUrl() != null) {
				currentPackage.setDocURL(new URL(getProject().getUrl()));
			}
		}

		if (currentPackage.getLicense() == null) {
			if (getProject().getLicenses() != null && !getProject().getLicenses().isEmpty()) {
				String lic = ((License) getProject().getLicenses().get(0)).getName();
				currentPackage.setLicense(lic);
			}
		}

		if (currentPackage.getName() == null) {
			if (getProject().getName() != null && !getProject().getName().startsWith("Unnamed")) {
				currentPackage.setName(getProject().getName());
			}
		}

		if (currentPackage.getVendor() == null) {
			if (getProject().getOrganization() != null) {
				currentPackage.setVendor(getProject().getOrganization().getName());
			}
		}

		List<Header> headers = dpInfo.getHeaders();

		// Adds header to the Deployment Package manifest
		for (Header header : headers) {		
			currentPackage.addManifestEntry(header.getName(), header.getValue());
		}

		// Write Extra Data ?
		if (writeExtraData) {
			currentPackage.addManifestEntry("Created-By",
			      System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
			currentPackage.addManifestEntry("Tool", PLUGIN_NAME);
			currentPackage.addManifestEntry("Created-At", "" + System.currentTimeMillis());
		}

	}

	/**
	 * @return the logger
	 * @see net.sourceforge.osgi.deployment.maven.IDeploymentPluginContext#getLogger()
	 */
	public final Log getLogger() {
		return super.getLog();
	}

	/**
	 * @return the outputDirectory
	 * @see net.sourceforge.osgi.deployment.maven.IDeploymentPluginContext#getOutputDirectory()
	 */
	public final File getOutputDirectory() {
		return ouputDirectory;
	}

	/**
	 * @param p_outputDirectory the outputDirectory to set
	 */
	public final void setOutputDirectory(final File p_outputDirectory) {
		ouputDirectory = p_outputDirectory;
	}

	/**
	 * @return the base directory
	 * @see net.sourceforge.osgi.deployment.maven.IDeploymentPluginContext#getBaseDir()
	 */
	public final File getBaseDir() {
		return basedir;
	}

	/**
	 * @return the menifest location
	 * @see net.sourceforge.osgi.deployment.maven.IDeploymentPluginContext#getManifestLocation()
	 */
	public final File getManifestLocation() {
		return manifestLocation;
	}

	/**
	 * @return the maven project
	 * @see net.sourceforge.osgi.deployment.maven.IDeploymentPluginContext#getProject()
	 */
	public final MavenProject getProject() {
		return project;
	}

	/**
	 * @return the build directory
	 * @see net.sourceforge.osgi.deployment.maven.IDeploymentPluginContext#getBuildDirectory()
	 */
	public final String getBuildDirectory() {
		return buildDirectory;
	}

	/**
	 * @return the supported packaging types
	 * @see net.sourceforge.osgi.deployment.maven.IDeploymentPluginContext#getSupportedProjectTypes()
	 */
	public final List<String> getSupportedProjectTypes() {
		return supportedProjectTypes;
	}

	/**
	 * @return the local repository
	 * @see net.sourceforge.osgi.deployment.maven.IDeploymentPluginContext#getLocalRepository()
	 */
	public final ArtifactRepository getLocalRepository() {
		return localRepository;
	}

	/**
	 * @return the remote repositories
	 * @see net.sourceforge.osgi.deployment.maven.IDeploymentPluginContext#getRemoteRepositories()
	 */
	public final List<ArtifactRepository> getRemoteRepositories() {
		return remoteRepositories;
	}

	/**
	 * @return the artifact factory
	 * @see net.sourceforge.osgi.deployment.maven.IDeploymentPluginContext#getArtifactFactory()
	 */
	public final ArtifactFactory getArtifactFactory() {
		return artifactFactory;
	}

	/**
	 * @return the artifact resolver
	 * @see net.sourceforge.osgi.deployment.maven.IDeploymentPluginContext#getArtifactResolver()
	 */
	public final ArtifactResolver getArtifactResolver() {
		return artifactresolver;
	}

	/**
	 * @return the artifact handler manager
	 * @see net.sourceforge.osgi.deployment.maven.IDeploymentPluginContext#getArtifactHandlerManager()
	 */
	public final ArtifactHandlerManager getArtifactHandlerManager() {
		return artifactHandlerManager;
	}

	/**
	 * @return the archiver manager
	 * @see net.sourceforge.osgi.deployment.maven.IDeploymentPluginContext#getArchiverManager()
	 */
	public final ArchiverManager getArchiverManager() {
		return archiverManager;
	}

	/**
	 * @return the deployment package info
	 * @see net.sourceforge.osgi.deployment.maven.IDeploymentPluginContext#getDeploymentPackageInfo()
	 */
	public final DeploymentPackageMetadata getDeploymentPackageInfo() {
		return deploymentPackageInfo;
	}

	/**
	 * @return <CODE>TRUE</CODE> if extra data should be generated into the manifest file, else <CODE>FALSE></CODE>.
	 *         Default is <CODE>TRUE</CODE>.
	 * @see net.sourceforge.osgi.deployment.maven.IDeploymentPluginContext#isWriteExtraData()
	 */
	public final boolean isWriteExtraData() {
		return writeExtraData;
	}

	/**
	 * @return <CODE>TRUE</CODE> if project dependencies should be included into the deployment package file, else
	 *         <CODE>FALSE></CODE>. Default is <CODE>TRUE</CODE>.
	 * @see net.sourceforge.osgi.deployment.maven.IDeploymentPluginContext#isWriteExtraData()
	 */
	public final boolean isIncludeDependencies() {
		return includeDependencies;
	}

	/**
	 * @param p_baseDir the baseDir to set
	 */
	public final void setBaseDir(final File p_baseDir) {
		basedir = p_baseDir;
	}

	/**
	 * @param p_manifestLocation the manifestLocation to set
	 */
	public final void setManifestLocation(final File p_manifestLocation) {
		manifestLocation = p_manifestLocation;
	}

	/**
	 * @param p_project the project to set
	 */
	public final void setProject(final MavenProject p_project) {
		project = p_project;
	}

	/**
	 * @param p_buildDirectory the buildDirectory to set
	 */
	public final void setBuildDirectory(final String p_buildDirectory) {
		buildDirectory = p_buildDirectory;
	}

	/**
	 * @param p_supportedProjectTypes the supportedProjectTypes to set
	 */
	public final void setSupportedProjectTypes(final List<String> p_supportedProjectTypes) {
		supportedProjectTypes = p_supportedProjectTypes;
	}

	/**
	 * @param p_localRepository the localRepository to set
	 */
	public final void setLocalRepository(final ArtifactRepository p_localRepository) {
		localRepository = p_localRepository;
	}

	/**
	 * @param p_remoteRepositories the remoteRepositories to set
	 */
	public final void setRemoteRepositories(final List<ArtifactRepository> p_remoteRepositories) {
		remoteRepositories = p_remoteRepositories;
	}

	/**
	 * @param p_artifactFactory the artifactFactory to set
	 */
	public final void setArtifactFactory(final ArtifactFactory p_artifactFactory) {
		artifactFactory = p_artifactFactory;
	}

	/**
	 * @param p_artifactResolver the artifactResolver to set
	 */
	public final void setArtifactResolver(final ArtifactResolver p_artifactResolver) {
		artifactresolver = p_artifactResolver;
	}

	/**
	 * @param p_artifactHandlerManager the artifactHandlerManager to set
	 */
	public final void setArtifactHandlerManager(final ArtifactHandlerManager p_artifactHandlerManager) {
		artifactHandlerManager = p_artifactHandlerManager;
	}

	/**
	 * @param p_archiverManager the archiverManager to set
	 */
	public final void setArchiverManager(final ArchiverManager p_archiverManager) {
		archiverManager = p_archiverManager;
	}

	/**
	 * @param p_deploymentPackageInfo the deploymentPackage to set
	 */
	public final void setDeploymentPackageInfo(final DeploymentPackageMetadata p_deploymentPackageInfo) {
		deploymentPackageInfo = p_deploymentPackageInfo;
	}

	/**
	 * @param p_deploymentPackageInfo the deploymentPackage to set
	 */
	public final void setDeploymentPackage(final DeploymentPackageMetadata p_deploymentPackageInfo) {
		setDeploymentPackageInfo(p_deploymentPackageInfo);
	}

	/**
	 * @param p_writeExtraData the writeExtraData to set
	 */
	public final void setWriteExtraData(final boolean p_writeExtraData) {
		writeExtraData = p_writeExtraData;
	}

	/**
	 * @param p_includeDependencies the includeDependencies to set
	 */
	public final void setIncludeDependencies(final boolean p_includeDependencies) {
		includeDependencies = p_includeDependencies;
	}

	/**
	 * This method resolves an artifact on all available repositories and returns the file handle to that artifact.
	 * 
	 * @param groupId the groupId of the artifact to resolve
	 * @param artifactId the artifactId of the artifact to resolve
	 * @param version the version of the artifact to resolve
	 * @param classifier the classifier of the artifact to resolve
	 * @return the resolved file handle of the artifact
	 * @throws MojoExecutionException
	 */
	public final File resolveResource(final String groupId, final String artifactId, final String version,
	      final String classifier) throws MojoExecutionException {
		try {

			if (artifactId == null)
				throw new MojoExecutionException("artifactId of artifact " + groupId + "::" + version + " must be defined");
			if (groupId == null)
				throw new MojoExecutionException("grouId of artifact :" + artifactId + ":" + version + " must be defined");

			String resolvedVersion = version;
			if (version == null) {
				List dependencies = project.getDependencies();
				if (dependencies != null) {
					for (Object depObj : dependencies) {
						if (!(depObj instanceof Dependency))
							continue;

						Dependency dep = (Dependency) depObj;
						if (!groupId.equals(dep.getGroupId()) || !artifactId.equals(dep.getArtifactId()))
							continue;

						resolvedVersion = dep.getVersion();
					}
				}
			}

			Artifact artifact = null;
			if (classifier == null) {
				artifact = getArtifactFactory().createArtifact(groupId, artifactId, resolvedVersion,
				      Artifact.SCOPE_RUNTIME, "jar");
			} else {
				artifact = getArtifactFactory().createArtifactWithClassifier(groupId, artifactId, resolvedVersion, "jar",
				      classifier);
			}

			getArtifactResolver().resolve(artifact, getRemoteRepositories(), getLocalRepository());
			final File artifactFile = artifact.getFile();
			return artifactFile;
		} catch (final Exception e) {
			// Wrap checked exception
			throw new MojoExecutionException("Error while resolving resource " + groupId + ":" + artifactId + ":"
			      + version, e);
		}
	}

}
