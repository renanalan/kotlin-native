package org.jetbrains.kotlin.backend.konan

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.backend.konan.llvm.loadMetadata
import org.jetbrains.kotlin.backend.konan.util.profile
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import java.io.File

class KonanConfig(val project: Project, val configuration: CompilerConfiguration) {

    internal val distribution = Distribution(configuration)

    internal val compileAsStdlib = configuration.get(KonanConfigKeys.COMPILE_AS_STDLIB) ?: false

    internal val libraries: List<String>
        get() {
            val fromCommandLine = configuration.getList(KonanConfigKeys.LIBRARY_FILES)
            if (configuration.get(KonanConfigKeys.NOSTDLIB) ?: false) {
                return fromCommandLine
            }
            return fromCommandLine + distribution.stdlib
        }

    private val loadedDescriptors = loadLibMetadata(libraries)

    internal val librariesToLink: List<String>
        get() = libraries + configuration.getList(KonanConfigKeys.NATIVE_LIBRARY_FILES)

    val moduleId: String
        get() = configuration.getNotNull(CommonConfigurationKeys.MODULE_NAME)

    fun loadLibMetadata(libraries: List<String>): List<ModuleDescriptorImpl> {

        val allMetadata = mutableListOf<ModuleDescriptorImpl>()

        for (path in libraries) {
            val filePath = File(path)
            if (!filePath.exists()) {
                error("Path '" + path + "' does not exist")
            }

            profile("Loading ${filePath}") {
                val moduleDescriptor = loadMetadata(configuration, filePath)
                allMetadata.add(moduleDescriptor)
            }
        }
        return allMetadata
    }

    internal val moduleDescriptors: List<ModuleDescriptorImpl> by lazy {
        for (module in loadedDescriptors) {
            // Yes, just to all of them.
            module.setDependencies(loadedDescriptors)
        }

        loadedDescriptors
    }
}
