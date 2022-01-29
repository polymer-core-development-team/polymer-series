import org.gradle.api.Action
import net.minecraftforge.gradle.userdev.UserDevExtension
import net.minecraftforge.gradle.userdev.DependencyManagementExtension
import net.minecraftforge.gradle.userdev.tasks.RenameJarInPlace
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.Project

val Project.minecraft: UserDevExtension get() =
    (this as ExtensionAware).extensions.getByName("minecraft") as UserDevExtension

fun Project.minecraft(configure: Action<UserDevExtension>): Unit =
    (this as ExtensionAware).extensions.configure("minecraft", configure)

val Project.fg: DependencyManagementExtension get() =
    (this as ExtensionAware).extensions.getByName("fg") as DependencyManagementExtension

val Project.reobf: NamedDomainObjectContainer<RenameJarInPlace> get() =
    (this as ExtensionAware).extensions.getByName("reobf") as NamedDomainObjectContainer<RenameJarInPlace>

fun Project.reobf(configure: Action<NamedDomainObjectContainer<RenameJarInPlace>>): Unit =
    (this as ExtensionAware).extensions.configure("reobf", configure)