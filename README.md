# Screen Time Lens

An Android app that shows which apps consume the most screen time today.

Screen Time Lens is a native Android application built with **Kotlin** and **Jetpack Compose**, following an MVVM architecture with a clean data/UI separation. It reads daily foreground usage through Android's **UsageStatsManager** API (with the required Usage Access permission flow and Android 11+ package visibility handling), resolves real app names and launcher icons via **PackageManager**, and exposes the result as immutable UI state using **Kotlin Coroutines** and **StateFlow**. The home screen renders a custom animated donut chart drawn on a Compose **Canvas**, with tap-to-inspect tooltips, in-chart percentage labels, and a ranked list of the top apps, all themed with **Material 3**.

**Tech stack:** Kotlin · Jetpack Compose · Material 3 · MVVM · ViewModel + StateFlow · Coroutines · UsageStatsManager · Canvas custom drawing · Gradle (Kotlin DSL)

<p align="center">
  <img src="https://i.hizliresim.com/gtn6zqdi.jpg" alt="Screenshot" width="500">
</p>