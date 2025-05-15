#include <jni.h>
#include <string>
#include <dlfcn.h>
#include <android/native_activity.h>
#include <GLES3/gl3.h>
#include <dobby.h>
#include "imgui.h"
#include "backends/imgui_impl_opengl3.h"
#include "backends/imgui_impl_android.h"


static void (*android_main_minecraft)(struct android_app *app);
static void (*ANativeActivity_onCreate_minecraft)(ANativeActivity *activity, void *savedState, size_t savedStateSize);
typedef EGLBoolean (*eglSwapBuffers_t)(EGLDisplay, EGLSurface);
eglSwapBuffers_t original_eglSwapBuffers;


EGLBoolean hooked_eglSwapBuffers(EGLDisplay display, EGLSurface surface) {
EGLBoolean result = original_eglSwapBuffers(display, surface);


ImGui_ImplOpenGL3_NewFrame();
ImGui::NewFrame();
ImGui::Begin("Taco Client");
ImGui::Text("Hello from Taco Client!");
ImGui::End();
ImGui::Render();
ImGui_ImplOpenGL3_RenderDrawData(ImGui::GetDrawData());

return result;
}


extern "C" void android_main(struct android_app *app) {
android_main_minecraft(app);
}


extern "C" void ANativeActivity_onCreate(ANativeActivity *activity, void *savedState, size_t savedStateSize) {
ANativeActivity_onCreate_minecraft(activity, savedState, savedStateSize);
}


JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
void *handle = dlopen("libminecraftpe.so", RTLD_LAZY);
android_main_minecraft = (void (*)(struct android_app *)) dlsym(handle, "android_main");
ANativeActivity_onCreate_minecraft = (void (*)(ANativeActivity *, void *, size_t)) dlsym(handle, "ANativeActivity_onCreate");

ImGui::CreateContext();
ImGui::StyleColorsDark();
ImGui_ImplOpenGL3_Init("#version 300 es");

void *eglSwapBuffers_addr = dlsym(RTLD_NEXT, "eglSwapBuffers");
if (eglSwapBuffers_addr) {
DobbyHook(eglSwapBuffers_addr, (void *)hooked_eglSwapBuffers, (void **)&original_eglSwapBuffers);
}

return JNI_VERSION_1_6;
}
