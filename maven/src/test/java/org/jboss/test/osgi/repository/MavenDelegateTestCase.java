package org.jboss.test.osgi.repository;

/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

import org.jboss.osgi.metadata.OSGiMetaData;
import org.jboss.osgi.metadata.OSGiMetaDataBuilder;
import org.jboss.osgi.repository.RequirementBuilder;
import org.jboss.osgi.repository.XRepository;
import org.jboss.osgi.repository.maven.MavenDelegateRepository;
import org.jboss.osgi.repository.maven.ShrinkwrapArtifactHandler;
import org.jboss.osgi.resolver.v2.XIdentityCapability;
import org.jboss.osgi.resolver.v2.XRequirement;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;
import org.osgi.framework.resource.Capability;

import java.io.InputStream;
import java.util.Collection;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import static org.junit.Assert.assertEquals;
import static org.osgi.framework.resource.ResourceConstants.IDENTITY_TYPE_BUNDLE;

/**
 * Test the default resolver integration.
 * 
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public class MavenDelegateTestCase  {

    private XRepository repository;

    @Before
    public void setUp() {
        repository = new MavenDelegateRepository(new ShrinkwrapArtifactHandler());
    }

    @Test
    public void testFindProvider() throws Exception {
        RequirementBuilder builder = repository.getRequirementBuilder();
        XRequirement req = builder.createArtifactRequirement("org.apache.felix:org.apache.felix.configadmin:1.2.8");
        verifyCapability(repository.findProvider(req));
    }

    @Test
    public void testFindProvidersByMavenId() throws Exception {
        RequirementBuilder builder = repository.getRequirementBuilder();
        XRequirement req = builder.createArtifactRequirement("org.apache.felix:org.apache.felix.configadmin:1.2.8");
        verifyProviders(repository.findProviders(req));
    }

    @Test
    public void testFindProvidersByArtifactId() throws Exception {
        RequirementBuilder builder = repository.getRequirementBuilder();
        XRequirement req = builder.createArtifactRequirement("org.apache.felix", "org.apache.felix.configadmin", "1.2.8");
        verifyProviders(repository.findProviders(req));
    }

    @Test
    public void testFindProvidersExplicit() throws Exception {
        RequirementBuilder builder = repository.getRequirementBuilder();
        XRequirement req = builder.createArtifactRequirement("org.apache.felix", "org.apache.felix.configadmin", "1.2.8", "jar", null);
        verifyProviders(repository.findProviders(req));
    }

    private void verifyProviders(Collection<Capability> caps) throws Exception {
        assertEquals("One capability", 1, caps.size());
        verifyCapability(caps.iterator().next());
    }

    private void verifyCapability(Capability cap) throws Exception {
        XIdentityCapability icap = (XIdentityCapability) cap;
        assertEquals("org.apache.felix.configadmin", icap.getSymbolicName());
        assertEquals(Version.parseVersion("1.2.8"), icap.getVersion());
        assertEquals(IDENTITY_TYPE_BUNDLE, icap.getType());

        InputStream content = icap.getResource().getContent();
        Manifest manifest = new JarInputStream(content).getManifest();
        OSGiMetaData metaData = OSGiMetaDataBuilder.load(manifest);
        assertEquals("org.apache.felix.configadmin", metaData.getBundleSymbolicName());
        assertEquals(Version.parseVersion("1.2.8"), metaData.getBundleVersion());
    }
}