#!/usr/bin/env python3
"""
Ars Deco Mod Deployment GUI
Python tkinter version with progress tracking and modern interface
"""

import tkinter as tk
from tkinter import ttk, messagebox
import subprocess
import os
import shutil
import threading
import time
from pathlib import Path
import logging

# Initialize logging to file
log_file = Path(__file__).resolve().parent / "deployment.log"
logging.basicConfig(
    filename=log_file,
    level=logging.DEBUG,
    format="%(asctime)s [%(levelname)s] %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S"
)

class DeploymentGUI:
    def __init__(self, root):
        self.root = root
        self.root.title("🚀 Ars Deco Deployment")
        self.root.geometry("600x400")
        self.root.resizable(False, False)
        
        # Center the window
        self.root.eval('tk::PlaceWindow . center')
        
        # Configuration
        self.instance_path = self.resolve_instance_path()
        self.project_dir = Path(__file__).resolve().parent
        self.build_libs_dir = self.project_dir / "build" / "libs"
        self.build_timeout_seconds = int(os.environ.get("ARS_DECO_BUILD_TIMEOUT", "1200"))
        self.file_retry_count = 8
        self.file_retry_delay_seconds = 1.0
        
        self.setup_ui()

    def resolve_instance_path(self):
        override = os.environ.get("ARS_DECO_INSTANCE_PATH")
        if override:
            return Path(override)

        instances_root = Path("C:/Users/harup/curseforge/minecraft/Instances")
        preferred_names = ["Ars Deco", "Ars-Deco"]
        for name in preferred_names:
            candidate = instances_root / name / "mods"
            if candidate.parent.exists():
                return candidate

        if instances_root.exists():
            for entry in instances_root.iterdir():
                normalized = entry.name.lower().replace("-", " ").replace("_", " ")
                if entry.is_dir() and "ars" in normalized and "deco" in normalized:
                    return entry / "mods"

        return instances_root / "Ars Deco" / "mods"
        
    def setup_ui(self):
        # Main frame
        main_frame = ttk.Frame(self.root, padding="20")
        main_frame.grid(row=0, column=0, sticky=(tk.W, tk.E, tk.N, tk.S))
        
        # Title
        title_label = ttk.Label(
            main_frame, 
            text="🚀 Ars Deco Mod Deployment",
            font=("Arial", 16, "bold")
        )
        title_label.grid(row=0, column=0, columnspan=2, pady=(0, 20))
        
        # Progress bar
        self.progress_var = tk.DoubleVar()
        self.progress_bar = ttk.Progressbar(
            main_frame,
            variable=self.progress_var,
            maximum=100,
            length=500,
            style="TProgressbar"
        )
        self.progress_bar.grid(row=1, column=0, columnspan=2, pady=(0, 10), sticky=(tk.W, tk.E))
        
        # Status label
        self.status_var = tk.StringVar(value="Ready to deploy")
        self.status_label = ttk.Label(
            main_frame,
            textvariable=self.status_var,
            font=("Arial", 12, "bold")
        )
        self.status_label.grid(row=2, column=0, columnspan=2, pady=(0, 10))
        
        # Detail label
        self.detail_var = tk.StringVar(value="Click Deploy to start building your mod")
        self.detail_label = ttk.Label(
            main_frame,
            textvariable=self.detail_var,
            font=("Arial", 9),
            foreground="gray"
        )
        self.detail_label.grid(row=3, column=0, columnspan=2, pady=(0, 20))
        
        # Log text area with scrollbar
        log_frame = ttk.Frame(main_frame)
        log_frame.grid(row=4, column=0, columnspan=2, sticky=(tk.W, tk.E, tk.N, tk.S), pady=(0, 20))
        
        self.log_text = tk.Text(
            log_frame,
            height=10,
            width=70,
            wrap=tk.WORD,
            font=("Consolas", 9),
            bg="#f8f9fa",
            fg="#212529"
        )
        scrollbar = ttk.Scrollbar(log_frame, orient=tk.VERTICAL, command=self.log_text.yview)
        self.log_text.configure(yscrollcommand=scrollbar.set)
        
        self.log_text.grid(row=0, column=0, sticky=(tk.W, tk.E, tk.N, tk.S))
        scrollbar.grid(row=0, column=1, sticky=(tk.N, tk.S))
        
        log_frame.grid_rowconfigure(0, weight=1)
        log_frame.grid_columnconfigure(0, weight=1)
        
        # Buttons
        button_frame = ttk.Frame(main_frame)
        button_frame.grid(row=5, column=0, columnspan=2, pady=(10, 0))
        
        self.deploy_button = ttk.Button(
            button_frame,
            text="🚀 Deploy Mod",
            command=self.start_deployment,
            width=15
        )
        self.deploy_button.pack(side=tk.LEFT, padx=(0, 10))
        
        self.cancel_button = ttk.Button(
            button_frame,
            text="❌ Cancel",
            command=self.cancel_deployment,
            state=tk.DISABLED,
            width=15
        )
        self.cancel_button.pack(side=tk.LEFT, padx=(0, 10))
        
        close_button = ttk.Button(
            button_frame,
            text="🚪 Close",
            command=self.root.quit,
            width=15
        )
        close_button.pack(side=tk.LEFT)
        
        # Configure grid weights
        self.root.grid_rowconfigure(0, weight=1)
        self.root.grid_columnconfigure(0, weight=1)
        main_frame.grid_rowconfigure(4, weight=1)
        main_frame.grid_columnconfigure(0, weight=1)
        
        # Initialize log
        self.log("🎯 Deployment GUI initialized")
        self.log(f"📁 Project directory: {self.project_dir}")
        self.log(f"📦 Target instance: {self.instance_path}")

    def gradle_build_command(self):
        java_home = os.environ.get("JAVA_HOME")
        if java_home:
            java_exe = Path(java_home) / "bin" / "java.exe"
            java_bin = str(java_exe) if java_exe.exists() else "java"
        else:
            java_bin = "java"

        wrapper_jar = self.project_dir / "gradle" / "wrapper" / "gradle-wrapper.jar"
        if not wrapper_jar.exists():
            raise FileNotFoundError(f"Missing Gradle wrapper jar: {wrapper_jar}")

        return [
            java_bin,
            "-classpath",
            str(wrapper_jar),
            "org.gradle.wrapper.GradleWrapperMain",
            "build",
            "--console=plain",
            "--no-daemon",
        ]

    def run_gradle_build(self):
        """Run build with live output and timeout."""
        command = self.gradle_build_command()
        self.log(f"⚙️ Build command: {' '.join(command)}")
        self.log(f"⏱️ Build timeout: {self.build_timeout_seconds}s")

        process = subprocess.Popen(
            command,
            cwd=self.project_dir,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
            creationflags=subprocess.CREATE_NO_WINDOW if os.name == 'nt' else 0,
            bufsize=1,
        )

        start = time.time()
        assert process.stdout is not None
        while True:
            line = process.stdout.readline()
            if line:
                self.log(line.rstrip())

            if process.poll() is not None:
                for remaining in process.stdout.readlines():
                    self.log(remaining.rstrip())
                return process.returncode

            if (time.time() - start) > self.build_timeout_seconds:
                process.terminate()
                try:
                    process.wait(timeout=10)
                except subprocess.TimeoutExpired:
                    process.kill()
                self.log("❌ Build timed out and was terminated")
                return 124
        
    def log(self, message):
        """Add a timestamped message to the log"""
        timestamp = time.strftime("%H:%M:%S")
        self.log_text.insert(tk.END, f"[{timestamp}] {message}\n")
        self.log_text.see(tk.END)
        self.root.update_idletasks()
        
        # Log to file
        logging.info(message)
        
    def update_progress(self, percentage, status, detail):
        """Update progress bar and status labels"""
        self.progress_var.set(percentage)
        self.status_var.set(status)
        self.detail_var.set(detail)
        self.root.update_idletasks()
        
    def start_deployment(self):
        """Start deployment in a separate thread"""
        self.deploy_button.config(state=tk.DISABLED)
        self.cancel_button.config(state=tk.NORMAL)
        self.deployment_thread = threading.Thread(target=self.deploy_mod, daemon=True)
        self.deployment_thread.start()
        
    def cancel_deployment(self):
        """Cancel the current deployment"""
        self.log("⚠️ Deployment cancellation requested")
        self.deploy_button.config(state=tk.NORMAL)
        self.cancel_button.config(state=tk.DISABLED)
        self.update_progress(0, "Cancelled", "Deployment was cancelled by user")
        
    def deploy_mod(self):
        """Main deployment logic"""
        try:
            self.log("🚀 Starting deployment process...")
            self.update_progress(5, "Initializing...", "Preparing deployment environment")
            time.sleep(0.5)
            
            # Check and create instance directory
            self.update_progress(10, "Checking directories...", "Verifying instance path")
            if not self.instance_path.exists():
                self.log(f"📁 Creating mods directory: {self.instance_path}")
                self.instance_path.mkdir(parents=True, exist_ok=True)
                self.update_progress(15, "Created directories", "Instance mods folder created")
            else:
                self.log("✅ Instance directory exists")
                
            # Start Gradle build
            self.update_progress(20, "Building mod...", "Running Gradle build process")
            self.log("⚙️ Starting Gradle build...")
            
            build_code = self.run_gradle_build()
            
            if build_code == 0:
                self.log("✅ Gradle build completed successfully")
                self.update_progress(60, "Build successful!", "Gradle build completed without errors")
                time.sleep(1)
                
                # Find jar file
                self.update_progress(70, "Finding jar file...", "Searching build/libs directory")
                jar_file = self.find_jar_file()
                
                if jar_file:
                    self.log(f"📦 Found jar: {jar_file.name}")
                    self.update_progress(80, f"Found: {jar_file.name}", "Located mod jar file")
                    time.sleep(0.5)
                    
                    # Clean old versions
                    self.update_progress(85, "Cleaning old versions...", "Removing previous mod files")
                    self.clean_old_versions()
                    
                    # Deploy new jar
                    self.update_progress(95, "Deploying mod...", "Copying jar to instance mods folder")
                    destination = self.instance_path / jar_file.name
                    self.copy_with_retry(jar_file, destination)
                    
                    self.log(f"🎯 Deployed to: {destination}")
                    self.update_progress(100, "✅ Deployment Complete!", "Mod successfully deployed")
                    
                    # Success styling
                    self.status_label.config(foreground="green")
                    
                    # Success dialog
                    messagebox.showinfo(
                        "✅ Deployment Successful",
                        f"Your mod has been deployed successfully!\n\n"
                        f"Location: {destination}\n\n"
                        f"You can now launch your Ars Deco instance!"
                    )
                    
                else:
                    raise Exception("No jar file found in build/libs directory")
                    
            else:
                raise Exception(f"Gradle build failed with exit code {build_code}")
                
        except Exception as e:
            self.log(f"❌ Error: {str(e)}")
            self.update_progress(self.progress_var.get(), "❌ Deployment Failed", f"Error: {str(e)}")
            
            # Error styling
            self.status_label.config(foreground="red")
            
            messagebox.showerror(
                "❌ Deployment Failed",
                f"Deployment failed with error:\n\n{str(e)}"
            )
            
        finally:
            self.deploy_button.config(state=tk.NORMAL)
            self.cancel_button.config(state=tk.DISABLED)
            
    def find_jar_file(self):
        """Find the most recent mod jar file"""
        if not self.build_libs_dir.exists():
            return None
            
        jar_files = [
            f for f in self.build_libs_dir.glob("*.jar")
            if "sources" not in f.name and "javadoc" not in f.name
        ]
        
        if jar_files:
            # Return the most recent file
            return max(jar_files, key=lambda f: f.stat().st_mtime)
        return None
        
    def clean_old_versions(self):
        """Remove old mod versions from the instance"""
        legacy_pattern = "an" + "_addon*.jar"
        patterns = ("ars_deco*.jar", legacy_pattern)
        for pattern in patterns:
            old_mods = list(self.instance_path.glob(pattern))
            for old_mod in old_mods:
                self.log(f"🗑️ Removing old version: {old_mod.name}")
                for attempt in range(self.file_retry_count):
                    try:
                        old_mod.unlink(missing_ok=True)
                        break
                    except PermissionError:
                        if attempt == self.file_retry_count - 1:
                            raise
                        time.sleep(self.file_retry_delay_seconds)

    def copy_with_retry(self, source, destination):
        for attempt in range(self.file_retry_count):
            try:
                shutil.copy2(source, destination)
                return
            except PermissionError:
                if attempt == self.file_retry_count - 1:
                    raise
                time.sleep(self.file_retry_delay_seconds)

def main():
    # Create and run the GUI
    root = tk.Tk()
    app = DeploymentGUI(root)
    root.mainloop()

if __name__ == "__main__":
    main()
