# Wright - VS Code Setup

This is a Minecraft Fabric mod project for Minecraft 1.21.1, now configured to work with Visual Studio Code.

## Prerequisites

1. **Java 21** - Required for Minecraft 1.21.1
2. **Visual Studio Code** with the following extensions:
   - Extension Pack for Java (redhat.java)
   - Gradle for Java (vscjava.vscode-gradle)

## Getting Started

1. Open this folder in VS Code
2. Open the workspace file: `wright-mod.code-workspace` for better organization
3. Wait for the Java extension to import the Gradle project (this may take a few minutes)
4. The project should automatically detect all dependencies and mappings

## Available Tasks (Ctrl+Shift+P → "Tasks: Run Task")

- **Build Mod** - Compiles the mod
- **Clean Build** - Clean and rebuild the mod  
- **Run Minecraft Client** - Starts Minecraft client with your mod loaded
- **Run Minecraft Server** - Starts Minecraft server with your mod loaded
- **Run Data Generation** - Generates mod data files (recipes, loot tables, etc.)
- **Generate Sources** - Generates Minecraft source mappings for development

## Debugging

For debugging your Fabric mod, use this workflow:

1. **Start Debug Session**:
   - Run the task: "Debug Minecraft Client" (Ctrl+Shift+P → Tasks: Run Task → Debug Minecraft Client)
   - This will start Minecraft with debug port 5005 open
   - Wait for the message "Listening for transport dt_socket at address: 5005"

2. **Attach Debugger**:
   - Press F5 or use the debug view
   - Select "Attach to Minecraft Client" 
   - VS Code will connect to the running Minecraft instance

3. **Set Breakpoints**:
   - Set breakpoints in your mod code
   - The debugger will pause execution when breakpoints are hit

Alternative: Use the regular "Run Minecraft Client" task for testing without debugging.

## Project Structure

```
src/main/
├── java/           # Your mod's Java code
├── resources/      # Mod resources (textures, models, etc.)
└── generated/      # Auto-generated files (don't edit manually)

.vscode/            # VS Code configuration
├── settings.json   # Workspace settings
├── tasks.json      # Build and run tasks
├── launch.json     # Debug configurations
└── extensions.json # Recommended extensions
```

## Building the Mod

- Use the "Build Mod" task (Ctrl+Shift+P → Tasks: Run Task → Build Mod)
- Or run `.\gradlew.bat build` in the terminal
- Built mod will be in `build/libs/`

## Development Workflow

1. **First time setup**: Run "Generate Sources" task to decompile Minecraft
2. **Daily development**: Use "Build Mod" to compile changes
3. **Testing**: Use "Run Minecraft Client" to test your mod
4. **Debugging**: Use F5 to start debugging with breakpoints

## VS Code Features

- **IntelliSense**: Full code completion for Minecraft and Fabric APIs
- **Error Detection**: Real-time error highlighting
- **Refactoring**: Rename symbols, extract methods, etc.
- **Navigation**: Go to definition (F12), find references (Shift+F12)
- **Hot Reload**: Debugging with live code updates
- **Integrated Terminal**: Built-in terminal for Gradle commands

## Tips

- The Java extension will automatically detect source changes
- Use Ctrl+Shift+O to navigate to symbols in current file
- Use Ctrl+P to quickly open files by name
- The Gradle extension provides build task integration in the sidebar
- Hot code replacement is enabled for faster debugging iterations

## Troubleshooting

If you encounter issues:

1. **Java not found**: Ensure Java 21 is installed and in PATH
2. **Project not importing**: Try "Java: Reload Projects" command (Ctrl+Shift+P)
3. **Build failures**: Run "Clean Build" task
4. **Mappings issues**: Run "Generate Sources" task
5. **VS Code not recognizing Java**: Install the Extension Pack for Java

## From IntelliJ IDEA

If you're migrating from IntelliJ IDEA:
- IntelliJ run configurations → VS Code launch configurations  
- IntelliJ project structure → Gradle handles dependencies
- IntelliJ debugger → VS Code Java debugger (F5)
- IntelliJ refactoring → Similar tools available in VS Code
