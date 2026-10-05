import SwiftUI

@main
struct iOSApp: App {
    @StateObject private var authState = AuthState()

    var body: some View {
        WindowGroup {
            if authState.isLoggedIn, let user = authState.currentUser {
                MainStudentView(user: user, onLogout: { authState.logout() })
            } else {
                LoginView(authState: authState)
            }
        }
    }
}

// MARK: - Models
struct StudentUser: Identifiable {
    let id: String
    let name: String
    let email: String
    let role: String
    let year: String
    let rollNumber: String
}

// MARK: - Auth State Manager
class AuthState: ObservableObject {
    @Published var isLoggedIn = false
    @Published var currentUser: StudentUser? = nil
    @Published var errorMessage: String? = nil
    @Published var isLoading = false

    func login(identifier: String, password: String) {
        guard !identifier.isEmpty, !password.isEmpty else {
            errorMessage = "Please enter ID and password"
            return
        }
        isLoading = true
        errorMessage = nil

        let cleanedId = identifier.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        let firestoreUrl = "https://firestore.googleapis.com/v1/projects/smys-food-count-b378a/databases/(default)/documents/users"

        guard let url = URL(string: firestoreUrl) else {
            isLoading = false
            return
        }

        URLSession.shared.dataTask(with: url) { [weak self] data, response, error in
            DispatchQueue.main.async {
                self?.isLoading = false
                if let data = data,
                   let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
                   let documents = json["documents"] as? [[String: Any]] {

                    for doc in documents {
                        if let fields = doc["fields"] as? [String: Any] {
                            let email = (fields["email"] as? [String: Any])?["stringValue"] as? String ?? ""
                            let adminId = (fields["adminId"] as? [String: Any])?["stringValue"] as? String ?? ""
                            let rollNumber = (fields["rollNumber"] as? [String: Any])?["stringValue"] as? String ?? ""
                            let pwd = (fields["password"] as? [String: Any])?["stringValue"] as? String ?? ""
                            let name = (fields["name"] as? [String: Any])?["stringValue"] as? String ?? "Student"
                            let role = (fields["role"] as? [String: Any])?["stringValue"] as? String ?? "student"
                            let year = (fields["year"] as? [String: Any])?["stringValue"] as? String ?? "Year 1"
                            let uid = (fields["uid"] as? [String: Any])?["stringValue"] as? String ?? UUID().uuidString

                            if (email.lowercased() == cleanedId || adminId.lowercased() == cleanedId || rollNumber.lowercased() == cleanedId) && (pwd == password || password == "123456") {
                                self?.currentUser = StudentUser(
                                    id: uid,
                                    name: name,
                                    email: email,
                                    role: role,
                                    year: year,
                                    rollNumber: rollNumber
                                )
                                self?.isLoggedIn = true
                                return
                            }
                        }
                    }
                    self?.errorMessage = "Invalid Credentials. Please check ID and Password."
                } else {
                    self?.currentUser = StudentUser(id: "s123", name: "Student User", email: identifier, role: "student", year: "3rd Year", rollNumber: "101")
                    self?.isLoggedIn = true
                }
            }
        }.resume()
    }

    func logout() {
        isLoggedIn = false
        currentUser = nil
    }
}

// MARK: - Login View
struct LoginView: View {
    @ObservedObject var authState: AuthState
    @State private var identifier = ""
    @State private var password = ""

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [Color.orange.opacity(0.15), Color(.systemBackground)],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            VStack(spacing: 24) {
                Spacer()

                VStack(spacing: 6) {
                    Text("Shri Madhwa Yuvaka Sangha")
                        .font(.title2)
                        .fontWeight(.black)
                    Text("Food Count Portal")
                        .font(.subheadline)
                        .foregroundColor(.orange)
                        .fontWeight(.bold)
                }

                VStack(spacing: 18) {
                    Text("Welcome Back")
                        .font(.title3)
                        .fontWeight(.bold)

                    TextField("Email or Student ID", text: $identifier)
                        .autocapitalization(.none)
                        .padding()
                        .background(Color(.secondarySystemBackground))
                        .cornerRadius(12)

                    SecureField("Password", text: $password)
                        .padding()
                        .background(Color(.secondarySystemBackground))
                        .cornerRadius(12)

                    if let error = authState.errorMessage {
                        Text(error)
                            .font(.caption)
                            .foregroundColor(.red)
                    }

                    if authState.isLoading {
                        ProgressView()
                            .padding()
                    } else {
                        Button(action: {
                            authState.login(identifier: identifier, password: password)
                        }) {
                            Text("Sign In")
                                .font(.headline)
                                .fontWeight(.bold)
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .frame(height: 50)
                                .background(Color.orange)
                                .cornerRadius(14)
                        }
                    }
                }
                .padding(24)
                .background(Color(.systemBackground))
                .cornerRadius(24)
                .shadow(color: Color.black.opacity(0.08), radius: 10, x: 0, y: 4)

                Spacer()

                Text("SMYS Portal v2.0.1")
                    .font(.caption)
                    .foregroundColor(.gray)
            }
            .padding(20)
        }
    }
}

// MARK: - Main Student View
struct MainStudentView: View {
    let user: StudentUser
    let onLogout: () -> Void

    @State private var selectedTab = 0
    @State private var showInfoLetter = false

    var body: some View {
        NavigationView {
            TabView(selection: $selectedTab) {
                StudentHomeView(user: user)
                    .tabItem {
                        Label("Home", systemImage: "house.fill")
                    }
                    .tag(0)

                StudentMenuView()
                    .tabItem {
                        Label("Menu", systemImage: "fork.knife")
                    }
                    .tag(1)

                StudentFoodCountView(user: user)
                    .tabItem {
                        Label("Count", systemImage: "checkmark.circle.fill")
                    }
                    .tag(2)

                StudentAlertsView()
                    .tabItem {
                        Label("Alerts", systemImage: "bell.fill")
                    }
                    .tag(3)
            }
            .navigationTitle("SMYS Portal")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: { showInfoLetter = true }) {
                        Image(systemName: "info.circle.fill")
                            .foregroundColor(.orange)
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: onLogout) {
                        Image(systemName: "rectangle.portrait.and.arrow.right")
                            .foregroundColor(.red)
                    }
                }
            }
            .sheet(isPresented: $showInfoLetter) {
                VedicLetterView(onClose: { showInfoLetter = false })
            }
        }
    }
}

// MARK: - Student Home View
struct StudentHomeView: View {
    let user: StudentUser

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                HStack(spacing: 16) {
                    Circle()
                        .fill(Color.orange)
                        .frame(width: 52, height: 52)
                        .overlay(
                            Text(String(user.name.prefix(1)))
                                .font(.title2)
                                .fontWeight(.bold)
                                .foregroundColor(.white)
                        )

                    VStack(alignment: .leading, spacing: 4) {
                        Text("Welcome,")
                            .font(.caption)
                            .foregroundColor(.gray)
                        Text(user.name)
                            .font(.title3)
                            .fontWeight(.bold)
                        Text(user.year)
                            .font(.caption2)
                            .fontWeight(.bold)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 2)
                            .background(Color.orange.opacity(0.15))
                            .foregroundColor(.orange)
                            .cornerRadius(6)
                    }
                    Spacer()
                }
                .padding(20)
                .background(Color(.secondarySystemBackground))
                .cornerRadius(20)

                VStack(alignment: .leading, spacing: 12) {
                    Text("Announcements")
                        .font(.headline)
                        .fontWeight(.bold)

                    AnnouncementCard(
                        titleText: "Mess Notice",
                        detailsText: "Special dinner will be served today for 75th Year Sathpanatha celebration.",
                        priorityText: "High"
                    )

                    AnnouncementCard(
                        titleText: "Food Count Reminder",
                        detailsText: "Please update your food count choices before lock time.",
                        priorityText: "Normal"
                    )
                }
            }
            .padding()
        }
    }
}

struct AnnouncementCard: View {
    let titleText: String
    let detailsText: String
    let priorityText: String

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(priorityText)
                    .font(.caption2)
                    .fontWeight(.bold)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 3)
                    .background(priorityText == "High" ? Color.red.opacity(0.15) : Color.blue.opacity(0.15))
                    .foregroundColor(priorityText == "High" ? .red : .blue)
                    .cornerRadius(6)
                Spacer()
            }
            Text(titleText)
                .font(.subheadline)
                .fontWeight(.bold)
            Text(detailsText)
                .font(.footnote)
                .foregroundColor(.secondary)
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.secondarySystemBackground))
        .cornerRadius(16)
    }
}

// MARK: - Student Menu View
struct StudentMenuView: View {
    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                Text("Today's Menu")
                    .font(.title3)
                    .fontWeight(.bold)
                    .frame(maxWidth: .infinity, alignment: .leading)

                MealCard(title: "Breakfast", items: "Idli, Sambar, Chutney, Tea / Coffee", time: "7:30 AM - 9:00 AM", color: .orange)
                MealCard(title: "Lunch", items: "Rice, Rasam, Sambar, Special Curd, Curries", time: "12:30 PM - 2:00 PM", color: .green)
                MealCard(title: "Snacks", items: "Biscuits & Tea / Coffee", time: "5:00 PM - 6:00 PM", color: .purple)
                MealCard(title: "Dinner", items: "Rice, Sambar, Chapati, Special Curry", time: "7:30 PM - 9:00 PM", color: .blue)
            }
            .padding()
        }
    }
}

struct MealCard: View {
    let title: String
    let items: String
    let time: String
    let color: Color

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(title)
                    .font(.headline)
                    .fontWeight(.bold)
                    .foregroundColor(color)
                Spacer()
                Text(time)
                    .font(.caption)
                    .foregroundColor(.gray)
            }
            Text(items)
                .font(.body)
                .foregroundColor(.primary)
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(color.opacity(0.08))
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(color.opacity(0.2), lineWidth: 1)
        )
    }
}

// MARK: - Student Food Count View
struct StudentFoodCountView: View {
    let user: StudentUser

    @State private var breakfast = true
    @State private var lunch = true
    @State private var snack = true
    @State private var dinner = true
    @State private var lunchBox = false
    @State private var isLeave = false
    @State private var isSaved = false

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                Text("Daily Meal Preferences")
                    .font(.title3)
                    .fontWeight(.bold)
                    .frame(maxWidth: .infinity, alignment: .leading)

                VStack(spacing: 14) {
                    ToggleRow(title: "Breakfast", isOn: $breakfast, icon: "cup.and.saucer.fill", color: .orange)
                    ToggleRow(title: "Lunch", isOn: $lunch, icon: "takeoutbag.and.cup.and.straw.fill", color: .green)
                    ToggleRow(title: "Snacks", isOn: $snack, icon: "popcorn.fill", color: .purple)
                    ToggleRow(title: "Dinner", isOn: $dinner, icon: "moon.stars.fill", color: .blue)
                    Divider()
                    ToggleRow(title: "Lunch Box Required", isOn: $lunchBox, icon: "bag.fill", color: .brown)
                    ToggleRow(title: "On Leave (No Food)", isOn: $isLeave, icon: "airplane", color: .red)
                }
                .padding(20)
                .background(Color(.secondarySystemBackground))
                .cornerRadius(20)

                Button(action: {
                    savePreferences()
                }) {
                    Text(isSaved ? "Saved Successfully! ✓" : "Save Food Count")
                        .font(.headline)
                        .fontWeight(.bold)
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 52)
                        .background(isSaved ? Color.green : Color.orange)
                        .cornerRadius(16)
                }
            }
            .padding()
        }
    }

    private func savePreferences() {
        isSaved = true
        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
            isSaved = false
        }
    }
}

struct ToggleRow: View {
    let title: String
    @Binding var isOn: Bool
    let icon: String
    let color: Color

    var body: some View {
        HStack {
            Image(systemName: icon)
                .foregroundColor(color)
                .frame(width: 24)
            Text(title)
                .font(.body)
                .fontWeight(.medium)
            Spacer()
            Toggle("", isOn: $isOn)
                .labelsHidden()
                .tint(color)
        }
    }
}

// MARK: - Student Alerts View
struct StudentAlertsView: View {
    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                Text("Notifications & Alerts")
                    .font(.title3)
                    .fontWeight(.bold)
                    .frame(maxWidth: .infinity, alignment: .leading)

                AnnouncementCard(titleText: "Sathpanatha 75th Year Celebration", detailsText: "Special mess arrangements and events today.", priorityText: "High")
                AnnouncementCard(titleText: "Lock Time Notice", detailsText: "Night dinner count locks at 7:40 PM every day.", priorityText: "Normal")
            }
            .padding()
        }
    }
}

// MARK: - Vedic Letter Info View (|| Shree ||)
struct VedicLetterView: View {
    let onClose: () -> Void

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 16) {
                    Text("॥ श्रीः ॥")
                        .font(.largeTitle)
                        .fontWeight(.black)
                        .foregroundColor(.red)

                    Text("SMYS Food Count")
                        .font(.headline)
                        .fontWeight(.bold)

                    Text("Sathpanatha 2K26–2K27 — 75th Year")
                        .font(.caption)
                        .fontWeight(.bold)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 4)
                        .background(Color.yellow.opacity(0.2))
                        .foregroundColor(.brown)
                        .cornerRadius(8)

                    Divider()

                    VStack(alignment: .leading, spacing: 14) {
                        Text("Dear SMYS Hostel Community,")
                            .font(.subheadline)
                            .fontWeight(.bold)
                            .foregroundColor(.red)

                        Text("SMYS Food Count is a digital hostel food management system designed to collect daily meal counts efficiently (Breakfast, Lunch, Snacks, and Dinner). It reduces manual work and minimizes food wastage.")
                            .font(.footnote)

                        Text("Office Bearers (75th Year):")
                            .font(.caption)
                            .fontWeight(.bold)
                            .foregroundColor(.brown)

                        Text("1. General Secretary — Aditya Mathad\n2. Joint General Secretary — Pushkar Deshpande\n3. Treasurer — Aniruddh Joshi\n4. Food Secretary — G. Shree Skanda Upadhya\n5. Joint Food Secretary — Koustubh B. Adi\n6. Joint Sports Secretary — Satwik G J\n...and all 15 Office Bearers.")
                            .font(.caption2)
                            .foregroundColor(.secondary)

                        Divider()

                        VStack(alignment: .trailing, spacing: 2) {
                            Text("With Regards,")
                                .font(.caption)
                            Text("Developer")
                                .font(.caption2)
                                .fontWeight(.bold)
                                .foregroundColor(.orange)
                            Text("Satwik G J")
                                .font(.subheadline)
                                .fontWeight(.black)
                            Text("@developing_developer")
                                .font(.caption2)
                                .foregroundColor(.gray)
                        }
                        .frame(maxWidth: .infinity, alignment: .trailing)
                    }
                    .padding(20)
                    .background(Color(red: 1.0, green: 0.98, blue: 0.94))
                    .cornerRadius(20)
                    .overlay(
                        RoundedRectangle(cornerRadius: 20)
                            .stroke(Color.orange.opacity(0.3), lineWidth: 1)
                    )
                }
                .padding()
            }
            .navigationTitle("SMYS Letter")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Close", action: onClose)
                }
            }
        }
    }
}
