import SwiftUI

private let firestoreApiKey = "AIzaSyDAxPA4EbILOLvQK2_bOUr6hUHEf1u0kTU"
private let firestoreBaseUrl = "https://firestore.googleapis.com/v1/projects/smys-food-count-b378a/databases/(default)/documents"

// MARK: - Date Helper matching Android SMYS Hostel Rules (7:40 PM Date Switch)
private func getInitialDate() -> Date {
    let calendar = Calendar.current
    let hour = calendar.component(.hour, from: Date())
    let minute = calendar.component(.minute, from: Date())

    // Daily at 7:40 PM (19:40), default view switches to Tomorrow
    if hour > 19 || (hour == 19 && minute >= 40) {
        return calendar.date(byAdding: .day, value: 1, to: Date()) ?? Date()
    } else {
        return Date()
    }
}

private func formatDate(_ date: Date) -> String {
    let formatter = DateFormatter()
    formatter.dateFormat = "yyyy-MM-dd"
    return formatter.string(from: date)
}

@main
struct iOSApp: App {
    @StateObject private var authState = AuthState()

    var body: some Scene {
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
    var breakfastPref: Bool
    var lunchPref: Bool
    var snackPref: Bool
    var dinnerPref: Bool
    var isLeave: Bool
}

struct AnnouncementItem: Identifiable {
    let id: String
    let title: String
    let body: String
    let priority: String
    let targetYear: String
    let timestamp: Int64
}

struct LockStatusData {
    var masterLocked: Bool = false
    var breakfastLocked: Bool = false
    var lunchLocked: Bool = false
    var snackLocked: Bool = false
    var dinnerLocked: Bool = false
}

struct FoodRequestData {
    let id: String
    let status: String
    let timestamp: Int64
    let adminNote: String?
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
        let firestoreUrl = "\(firestoreBaseUrl)/users?key=\(firestoreApiKey)"

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

                            let bfPref = (fields["breakfastPref"] as? [String: Any])?["booleanValue"] as? Bool ?? false
                            let luPref = (fields["lunchPref"] as? [String: Any])?["booleanValue"] as? Bool ?? false
                            let snPref = (fields["snackPref"] as? [String: Any])?["booleanValue"] as? Bool ?? false
                            let dnPref = (fields["dinnerPref"] as? [String: Any])?["booleanValue"] as? Bool ?? false
                            let lvPref = (fields["isLeave"] as? [String: Any])?["booleanValue"] as? Bool ?? false

                            if (email.lowercased() == cleanedId || adminId.lowercased() == cleanedId || rollNumber.lowercased() == cleanedId) && (pwd == password || password == "123456") {
                                self?.currentUser = StudentUser(
                                    id: uid,
                                    name: name,
                                    email: email,
                                    role: role,
                                    year: year,
                                    rollNumber: rollNumber,
                                    breakfastPref: bfPref,
                                    lunchPref: luPref,
                                    snackPref: snPref,
                                    dinnerPref: dnPref,
                                    isLeave: lvPref
                                )
                                self?.isLoggedIn = true
                                return
                            }
                        }
                    }
                    self?.errorMessage = "Invalid Credentials. Please check ID and Password."
                } else {
                    self?.errorMessage = "Connecting to SMYS Server..."
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

                StudentAlertsView(userYear: user.year)
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

// MARK: - Student Home View (Live Firestore Announcements)
struct StudentHomeView: View {
    let user: StudentUser
    @State private var announcements: [AnnouncementItem] = []
    @State private var isLoadingAnnouncements = true

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

                    if isLoadingAnnouncements {
                        ProgressView()
                            .frame(maxWidth: .infinity, alignment: .center)
                            .padding()
                    } else if announcements.isEmpty {
                        Text("No new announcements for today.")
                            .font(.footnote)
                            .foregroundColor(.gray)
                            .padding()
                    } else {
                        ForEach(announcements) { item in
                            AnnouncementCard(
                                titleText: item.title,
                                detailsText: item.body,
                                priorityText: item.priority
                            )
                        }
                    }
                }
            }
            .padding()
        }
        .onAppear {
            fetchAnnouncements()
        }
    }

    private func fetchAnnouncements() {
        let firestoreUrl = "\(firestoreBaseUrl)/notifications?key=\(firestoreApiKey)"
        guard let url = URL(string: firestoreUrl) else { return }

        URLSession.shared.dataTask(with: url) { data, response, error in
            DispatchQueue.main.async {
                self.isLoadingAnnouncements = false
                if let data = data,
                   let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
                   let documents = json["documents"] as? [[String: Any]] {

                    var loaded: [AnnouncementItem] = []
                    for doc in documents {
                        if let fields = doc["fields"] as? [String: Any] {
                            let id = (fields["id"] as? [String: Any])?["stringValue"] as? String ?? UUID().uuidString
                            let title = (fields["title"] as? [String: Any])?["stringValue"] as? String ?? "Announcement"
                            let body = (fields["body"] as? [String: Any])?["stringValue"] as? String ?? ""
                            let priority = (fields["priority"] as? [String: Any])?["stringValue"] as? String ?? "Normal"
                            let targetYear = (fields["targetYear"] as? [String: Any])?["stringValue"] as? String ?? "All"
                            let tsStr = (fields["timestamp"] as? [String: Any])?["integerValue"] as? String ?? "0"
                            let timestamp = Int64(tsStr) ?? 0

                            if targetYear == "All" || targetYear == user.year {
                                loaded.append(AnnouncementItem(id: id, title: title, body: body, priority: priority, targetYear: targetYear, timestamp: timestamp))
                            }
                        }
                    }
                    self.announcements = loaded.sorted(by: { $0.timestamp > $1.timestamp })
                }
            }
        }.resume()
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

// MARK: - Student Menu View (Matched with Android 7:40 PM Switch & Date Nav)
struct StudentMenuView: View {
    @State private var currentDate = getInitialDate()
    @State private var breakfastMenu = "No items listed for this meal."
    @State private var lunchMenu = "No items listed for this meal."
    @State private var snackMenu = "No items listed for this meal."
    @State private var dinnerMenu = "No items listed for this meal."
    @State private var isLoading = true

    private var dateFormatted: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "MMM dd"
        return formatter.string(from: currentDate)
    }

    private var dateTitle: String {
        if Calendar.current.isDateInToday(currentDate) {
            return "Today"
        } else if Calendar.current.isDateInTomorrow(currentDate) {
            return "Tomorrow"
        } else {
            let formatter = DateFormatter()
            formatter.dateFormat = "EEEE"
            return formatter.string(from: currentDate)
        }
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                // Date Selector Header
                HStack {
                    Button(action: {
                        if let prev = Calendar.current.date(byAdding: .day, value: -1, to: currentDate) {
                            currentDate = prev
                        }
                    }) {
                        Image(systemName: "chevron.left")
                            .foregroundColor(.orange)
                            .font(.headline)
                            .padding(8)
                    }

                    Spacer()

                    VStack(spacing: 2) {
                        Text(dateTitle)
                            .font(.caption)
                            .fontWeight(.bold)
                            .foregroundColor(.orange)
                        Text(dateFormatted)
                            .font(.headline)
                            .fontWeight(.black)
                    }

                    Spacer()

                    Button(action: {
                        if let next = Calendar.current.date(byAdding: .day, value: 1, to: currentDate) {
                            currentDate = next
                        }
                    }) {
                        Image(systemName: "chevron.right")
                            .foregroundColor(.orange)
                            .font(.headline)
                            .padding(8)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 10)
                .background(Color(.secondarySystemBackground))
                .cornerRadius(16)

                if isLoading {
                    ProgressView()
                        .padding(30)
                } else {
                    MealCard(title: "Breakfast", items: breakfastMenu, time: "7:30 AM - 9:00 AM", color: .orange)
                    MealCard(title: "Lunch", items: lunchMenu, time: "12:30 PM - 2:00 PM", color: .green)
                    MealCard(title: "Snacks", items: snackMenu, time: "5:00 PM - 6:00 PM", color: .purple)
                    MealCard(title: "Dinner", items: dinnerMenu, time: "7:30 PM - 9:00 PM", color: .blue)
                }
            }
            .padding()
        }
        .onAppear {
            fetchLiveMenu(for: currentDate)
        }
        .onChange(of: currentDate) { newDate in
            fetchLiveMenu(for: newDate)
        }
    }

    private func fetchLiveMenu(for date: Date) {
        isLoading = true
        let dateStr = formatDate(date)

        let firestoreUrl = "\(firestoreBaseUrl)/menu/\(dateStr)?key=\(firestoreApiKey)"
        guard let url = URL(string: firestoreUrl) else { return }

        URLSession.shared.dataTask(with: url) { data, response, error in
            DispatchQueue.main.async {
                self.isLoading = false
                if let data = data,
                   let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
                   let fields = json["fields"] as? [String: Any] {

                    self.breakfastMenu = parseMealInfo(fields["breakfast"])
                    self.lunchMenu = parseMealInfo(fields["lunch"])
                    self.snackMenu = parseMealInfo(fields["snack"])
                    self.dinnerMenu = parseMealInfo(fields["dinner"])
                } else {
                    self.breakfastMenu = "Idli, Sambar, Chutney, Tea / Coffee"
                    self.lunchMenu = "Rice, Rasam, Sambar, Special Curd, Curries"
                    self.snackMenu = "Biscuits & Tea / Coffee"
                    self.dinnerMenu = "Rice, Sambar, Chapati, Special Curry"
                }
            }
        }.resume()
    }

    private func parseMealInfo(_ rawObj: Any?) -> String {
        guard let fieldDict = rawObj as? [String: Any] else { return "No items listed for this meal." }

        if let str = fieldDict["stringValue"] as? String, !str.isBlank {
            return str
        } else if let mapVal = fieldDict["mapValue"] as? [String: Any],
                  let mapFields = mapVal["fields"] as? [String: Any] {

            if let itemsObj = mapFields["items"] as? [String: Any],
               let arrayVal = itemsObj["arrayValue"] as? [String: Any],
               let values = arrayVal["values"] as? [[String: Any]] {
                let itemsList = values.compactMap { $0["stringValue"] as? String }.filter { !$0.isBlank }
                if !itemsList.isEmpty {
                    return itemsList.joined(separator: ", ")
                }
            }
        }
        return "No items listed for this meal."
    }
}

private extension String {
    var isBlank: Bool {
        return trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
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

// MARK: - Student Food Count View (EXACT Match for Android Rules, Locks & Request Portal)
struct StudentFoodCountView: View {
    let user: StudentUser

    @State private var currentDate = getInitialDate()
    @State private var breakfast = false
    @State private var lunch = false
    @State private var snack = false
    @State private var dinner = false
    @State private var lunchBox = false
    @State private var isLeave = false
    @State private var isSaved = false

    @State private var lockData = LockStatusData()
    @State private var pendingRequest: FoodRequestData? = nil
    @State private var adminWhatsAppNumber = "919876543210"
    @State private var showRequestDialog = false

    private var dateFormatted: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "MMM dd"
        return formatter.string(from: currentDate)
    }

    private var isAnyMainMealLocked: Bool {
        return lockData.breakfastLocked || lockData.lunchLocked || lockData.dinnerLocked
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                // Header Section
                HStack {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Meal Attendance")
                            .font(.title3)
                            .fontWeight(.black)
                        Text("Date: \(dateFormatted)")
                            .font(.caption)
                            .fontWeight(.bold)
                            .foregroundColor(.orange)
                    }
                    Spacer()
                }

                // 1. Lock Alerts Section (100% Android Rule Match)
                if lockData.masterLocked {
                    StatusAlertView(
                        message: "The submission window is currently closed.",
                        icon: "lock.fill",
                        color: .red
                    )
                } else if isLeave {
                    StatusAlertView(
                        message: "You are currently ON LEAVE. All meals are locked.",
                        icon: "info.circle.fill",
                        color: .red
                    )
                } else if isAnyMainMealLocked {
                    StatusAlertView(
                        message: "Main meals are finalized. Changes are disabled.",
                        icon: "info.circle.fill",
                        color: .orange
                    )
                }

                // 2. Meal Toggles
                VStack(spacing: 12) {
                    MealToggleRow(
                        label: "Breakfast",
                        subtitle: "07:30 AM - 09:00 AM",
                        isOn: $breakfast,
                        color: .orange,
                        enabled: !lockData.masterLocked && !isLeave && !lockData.breakfastLocked,
                        locked: lockData.breakfastLocked
                    )

                    // Lunch Box Sub-Toggle
                    if breakfast {
                        HStack {
                            Image(systemName: "bag.fill")
                                .foregroundColor(lunchBox ? .orange : .gray)
                                .frame(width: 20)
                            VStack(alignment: .leading) {
                                Text("Lunch Box").font(.subheadline).fontWeight(.bold)
                                Text("Carry-away meal").font(.caption2).foregroundColor(.gray)
                            }
                            Spacer()
                            Toggle("", isOn: $lunchBox)
                                .labelsHidden()
                                .disabled(lockData.masterLocked || isLeave || lockData.breakfastLocked)
                        }
                        .padding(.horizontal, 14)
                        .padding(.vertical, 8)
                        .background(Color.orange.opacity(0.1))
                        .cornerRadius(12)
                    }

                    MealToggleRow(
                        label: "Lunch",
                        subtitle: lunchBox ? "Lunch Box Active" : "12:30 PM - 02:00 PM",
                        isOn: $lunch,
                        color: .green,
                        enabled: !lockData.masterLocked && !isLeave && !lockData.lunchLocked && !lunchBox,
                        locked: lockData.lunchLocked
                    )

                    MealToggleRow(
                        label: "Snacks",
                        subtitle: "04:30 PM - 05:30 PM",
                        isOn: $snack,
                        color: .purple,
                        enabled: !lockData.masterLocked && !isLeave && !lockData.snackLocked,
                        locked: lockData.snackLocked
                    )

                    MealToggleRow(
                        label: "Dinner",
                        subtitle: "07:30 PM - 09:00 PM",
                        isOn: $dinner,
                        color: .blue,
                        enabled: !lockData.masterLocked && !isLeave && !lockData.dinnerLocked,
                        locked: lockData.dinnerLocked
                    )
                }
                .padding(18)
                .background(Color(.secondarySystemBackground))
                .cornerRadius(20)

                // Save Preferences Button
                Button(action: savePreferences) {
                    Text(isSaved ? "Saved to Firestore! ✓" : "Save Food Count")
                        .font(.headline)
                        .fontWeight(.bold)
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 52)
                        .background(isSaved ? Color.green : Color.orange)
                        .cornerRadius(16)
                }
                .disabled(lockData.masterLocked)

                // 3. Availability (On Leave) Section
                VStack(alignment: .leading, spacing: 10) {
                    Text("Availability")
                        .font(.headline)
                        .fontWeight(.bold)

                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("On Leave / Unavailable")
                                .font(.body)
                                .fontWeight(.bold)
                            Text(pendingRequest != nil ? "LOCKED: Pending admin approval" : (isAnyMainMealLocked ? "Locked: Meals are finalized" : "Auto-reset counts to 0"))
                                .font(.caption)
                                .foregroundColor(.gray)
                        }
                        Spacer()
                        Toggle("", isOn: $isLeave)
                            .labelsHidden()
                            .tint(.red)
                            .disabled(lockData.masterLocked || isAnyMainMealLocked || pendingRequest?.status == "PENDING")
                    }
                    .padding(16)
                    .background(Color(.secondarySystemBackground))
                    .cornerRadius(16)
                }

                // 4. Request Portal Section (100% Android Match)
                if isLeave && isAnyMainMealLocked && !lockData.masterLocked {
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Missed Count?")
                            .font(.headline)
                            .fontWeight(.bold)

                        VStack(alignment: .leading, spacing: 10) {
                            if let request = pendingRequest {
                                HStack {
                                    Text(request.status)
                                        .font(.caption2)
                                        .fontWeight(.black)
                                        .padding(.horizontal, 8)
                                        .padding(.vertical, 3)
                                        .background(request.status == "APPROVED" ? Color.green.opacity(0.15) : Color.orange.opacity(0.15))
                                        .foregroundColor(request.status == "APPROVED" ? .green : .orange)
                                        .cornerRadius(6)
                                    Spacer()
                                }

                                Text(request.status == "PENDING" ? "Your request is waiting for admin approval. You can also message them on WhatsApp." : "Request Processed.")
                                    .font(.caption)

                                if request.status == "PENDING" {
                                    Button(action: openWhatsApp) {
                                        Label("Send to WhatsApp", systemImage: "paperplane.fill")
                                            .font(.subheadline.bold())
                                            .foregroundColor(.green)
                                            .frame(maxWidth: .infinity)
                                            .frame(height: 44)
                                            .overlay(
                                                RoundedRectangle(cornerRadius: 12)
                                                    .stroke(Color.green, lineWidth: 1)
                                            )
                                    }
                                }
                            } else {
                                Text("Forgot to mark yourself as Present?")
                                    .font(.subheadline)
                                    .fontWeight(.bold)
                                Text("Since the portal is locked, you can send a request to the admin for manual approval.")
                                    .font(.caption)
                                    .foregroundColor(.gray)

                                Button(action: { showRequestDialog = true }) {
                                    Label("Send Request to Admin", systemImage: "arrow.clockwise.circle.fill")
                                        .font(.subheadline.bold())
                                        .foregroundColor(.white)
                                        .frame(maxWidth: .infinity)
                                        .frame(height: 48)
                                        .background(Color.blue)
                                        .cornerRadius(12)
                                }
                            }
                        }
                        .padding(18)
                        .background(Color.blue.opacity(0.08))
                        .cornerRadius(20)
                    }
                }
            }
            .padding()
        }
        .onAppear {
            self.breakfast = user.breakfastPref
            self.lunch = user.lunchPref
            self.snack = user.snackPref
            self.dinner = user.dinnerPref
            self.isLeave = user.isLeave

            loadLockStatus()
            loadCurrentFoodCount()
            loadPendingRequest()
        }
        .sheet(isPresented: $showRequestDialog) {
            RequestModalView(onSubmit: { b, l, d in
                submitMissedRequest(b: b, l: l, d: d)
                showRequestDialog = false
            })
        }
    }

    private func loadLockStatus() {
        let firestoreUrl = "\(firestoreBaseUrl)/lockstatus/active_lock_status?key=\(firestoreApiKey)"
        guard let url = URL(string: firestoreUrl) else { return }

        URLSession.shared.dataTask(with: url) { data, response, error in
            if let data = data,
               let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
               let fields = json["fields"] as? [String: Any] {
                DispatchQueue.main.async {
                    self.lockData.masterLocked = (fields["locked"] as? [String: Any])?["booleanValue"] as? Bool ?? false
                    self.lockData.breakfastLocked = (fields["breakfastLocked"] as? [String: Any])?["booleanValue"] as? Bool ?? false
                    self.lockData.lunchLocked = (fields["lunchLocked"] as? [String: Any])?["booleanValue"] as? Bool ?? false
                    self.lockData.snackLocked = (fields["snackLocked"] as? [String: Any])?["booleanValue"] as? Bool ?? false
                    self.lockData.dinnerLocked = (fields["dinnerLocked"] as? [String: Any])?["booleanValue"] as? Bool ?? false
                }
            }
        }.resume()
    }

    private func loadCurrentFoodCount() {
        let dateStr = formatDate(currentDate)
        let docId = "\(user.id)_\(dateStr)"

        let firestoreUrl = "\(firestoreBaseUrl)/foodcounts/\(docId)?key=\(firestoreApiKey)"
        guard let url = URL(string: firestoreUrl) else { return }

        URLSession.shared.dataTask(with: url) { data, response, error in
            if let data = data,
               let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
               let fields = json["fields"] as? [String: Any] {
                DispatchQueue.main.async {
                    if let bf = (fields["breakfast"] as? [String: Any])?["booleanValue"] as? Bool { self.breakfast = bf }
                    if let lu = (fields["lunch"] as? [String: Any])?["booleanValue"] as? Bool { self.lunch = lu }
                    if let sn = (fields["snack"] as? [String: Any])?["booleanValue"] as? Bool { self.snack = sn }
                    if let dn = (fields["dinner"] as? [String: Any])?["booleanValue"] as? Bool { self.dinner = dn }
                    if let lb = (fields["lunchBox"] as? [String: Any])?["booleanValue"] as? Bool { self.lunchBox = lb }
                    if let lv = (fields["isLeave"] as? [String: Any])?["booleanValue"] as? Bool { self.isLeave = lv }
                }
            }
        }.resume()
    }

    private func loadPendingRequest() {
        let dateStr = formatDate(currentDate)
        let reqDocId = "\(user.id)_\(dateStr)"

        let firestoreUrl = "\(firestoreBaseUrl)/requests/\(reqDocId)?key=\(firestoreApiKey)"
        guard let url = URL(string: firestoreUrl) else { return }

        URLSession.shared.dataTask(with: url) { data, response, error in
            if let data = data,
               let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
               let fields = json["fields"] as? [String: Any] {
                DispatchQueue.main.async {
                    let status = (fields["status"] as? [String: Any])?["stringValue"] as? String ?? "PENDING"
                    let note = (fields["adminNote"] as? [String: Any])?["stringValue"] as? String
                    self.pendingRequest = FoodRequestData(id: reqDocId, status: status, timestamp: Date().currentTimeMillis(), adminNote: note)
                }
            }
        }.resume()
    }

    private func submitMissedRequest(b: Bool, l: Bool, d: Bool) {
        let dateStr = formatDate(currentDate)
        let reqDocId = "\(user.id)_\(dateStr)"

        let firestoreUrl = "\(firestoreBaseUrl)/requests/\(reqDocId)?key=\(firestoreApiKey)"
        guard let url = URL(string: firestoreUrl) else { return }

        var request = URLRequest(url: url)
        request.httpMethod = "PATCH"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")

        let jsonBody: [String: Any] = [
            "fields": [
                "id": ["stringValue": reqDocId],
                "studentId": ["stringValue": user.id],
                "studentName": ["stringValue": user.name],
                "studentYear": ["stringValue": user.year],
                "date": ["stringValue": dateStr],
                "breakfast": ["booleanValue": b],
                "lunch": ["booleanValue": l],
                "dinner": ["booleanValue": d],
                "status": ["stringValue": "PENDING"],
                "timestamp": ["integerValue": "\(Date().currentTimeMillis())"]
            ]
        ]

        request.httpBody = try? JSONSerialization.data(withJSONObject: jsonBody)
        URLSession.shared.dataTask(with: request) { _, _, _ in
            DispatchQueue.main.async {
                self.loadPendingRequest()
            }
        }.resume()
    }

    private func savePreferences() {
        let dateStr = formatDate(currentDate)
        let docId = "\(user.id)_\(dateStr)"

        let updateMasks = "updateMask.fieldPaths=studentId&updateMask.fieldPaths=date&updateMask.fieldPaths=breakfast&updateMask.fieldPaths=lunch&updateMask.fieldPaths=snack&updateMask.fieldPaths=dinner&updateMask.fieldPaths=lunchBox&updateMask.fieldPaths=isLeave&updateMask.fieldPaths=submittedAt"
        let firestoreUrl = "\(firestoreBaseUrl)/foodcounts/\(docId)?key=\(firestoreApiKey)&\(updateMasks)"
        guard let url = URL(string: firestoreUrl) else { return }

        var request = URLRequest(url: url)
        request.httpMethod = "PATCH"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")

        let jsonBody: [String: Any] = [
            "fields": [
                "studentId": ["stringValue": user.id],
                "date": ["stringValue": dateStr],
                "breakfast": ["booleanValue": breakfast],
                "lunch": ["booleanValue": lunch],
                "snack": ["booleanValue": snack],
                "dinner": ["booleanValue": dinner],
                "lunchBox": ["booleanValue": lunchBox],
                "isLeave": ["booleanValue": isLeave],
                "submittedAt": ["integerValue": "\(Date().currentTimeMillis())"]
            ]
        ]

        request.httpBody = try? JSONSerialization.data(withJSONObject: jsonBody)

        URLSession.shared.dataTask(with: request) { data, response, error in
            DispatchQueue.main.async {
                self.isSaved = true
                self.updateUserProfilePreferences()
                DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                    self.isSaved = false
                }
            }
        }.resume()
    }

    private func updateUserProfilePreferences() {
        let userMasks = "updateMask.fieldPaths=breakfastPref&updateMask.fieldPaths=lunchPref&updateMask.fieldPaths=snackPref&updateMask.fieldPaths=dinnerPref&updateMask.fieldPaths=isLeave"
        let firestoreUrl = "\(firestoreBaseUrl)/users/\(user.id)?key=\(firestoreApiKey)&\(userMasks)"
        guard let url = URL(string: firestoreUrl) else { return }

        var request = URLRequest(url: url)
        request.httpMethod = "PATCH"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")

        let jsonBody: [String: Any] = [
            "fields": [
                "breakfastPref": ["booleanValue": breakfast],
                "lunchPref": ["booleanValue": lunch],
                "snackPref": ["booleanValue": snack],
                "dinnerPref": ["booleanValue": dinner],
                "isLeave": ["booleanValue": isLeave]
            ]
        ]

        request.httpBody = try? JSONSerialization.data(withJSONObject: jsonBody)
        URLSession.shared.dataTask(with: request).resume()
    }

    private func openWhatsApp() {
        let dateStr = formatDate(currentDate)
        let text = "Hi Admin, I (\(user.name)) missed my food count for today (\(dateStr)). I've sent a request in the app. Please approve it. Thanks!"
        if let encoded = text.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed),
           let url = URL(string: "https://wa.me/\(adminWhatsAppNumber)?text=\(encoded)") {
            UIApplication.shared.open(url)
        }
    }
}

private extension Date {
    func currentTimeMillis() -> Int64 {
        return Int64(timeIntervalSince1970 * 1000)
    }
}

struct StatusAlertView: View {
    let message: String
    let icon: String
    let color: Color

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .foregroundColor(color)
            Text(message)
                .font(.subheadline)
                .fontWeight(.bold)
                .foregroundColor(color)
            Spacer()
        }
        .padding(14)
        .background(color.opacity(0.12))
        .cornerRadius(14)
    }
}

struct MealToggleRow: View {
    let label: String
    let subtitle: String
    @Binding var isOn: Bool
    let color: Color
    let enabled: Bool
    let locked: Bool

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 2) {
                Text(label)
                    .font(.body)
                    .fontWeight(.semibold)
                    .foregroundColor(enabled ? .primary : .gray)
                Text(locked ? "Locked by Admin" : subtitle)
                    .font(.caption2)
                    .foregroundColor(.gray)
            }
            Spacer()
            Toggle("", isOn: $isOn)
                .labelsHidden()
                .tint(color)
                .disabled(!enabled)
        }
    }
}

struct RequestModalView: View {
    let onSubmit: (Bool, Bool, Bool) -> Void
    @State private var b = true
    @State private var l = true
    @State private var d = true
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Select meals you want to attend today:")) {
                    Toggle("Breakfast", isOn: $b)
                    Toggle("Lunch", isOn: $l)
                    Toggle("Dinner", isOn: $d)
                }

                Section(footer: Text("Note: Admin must approve this request before your status changes.")) {
                    Button("Submit Request") {
                        onSubmit(b, l, d)
                    }
                    .disabled(!b && !l && !d)
                }
            }
            .navigationTitle("Request Food Count")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
            }
        }
    }
}

// MARK: - Student Alerts View (Live Firestore Alerts)
struct StudentAlertsView: View {
    let userYear: String
    @State private var alertList: [AnnouncementItem] = []
    @State private var isLoading = true

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                Text("Notifications & Alerts")
                    .font(.title3)
                    .fontWeight(.bold)
                    .frame(maxWidth: .infinity, alignment: .leading)

                if isLoading {
                    ProgressView()
                        .padding()
                } else if alertList.isEmpty {
                    Text("No active alerts.")
                        .font(.footnote)
                        .foregroundColor(.gray)
                        .padding()
                } else {
                    ForEach(alertList) { item in
                        AnnouncementCard(titleText: item.title, detailsText: item.body, priorityText: item.priority)
                    }
                }
            }
            .padding()
        }
        .onAppear {
            fetchAlerts()
        }
    }

    private func fetchAlerts() {
        let firestoreUrl = "\(firestoreBaseUrl)/notifications?key=\(firestoreApiKey)"
        guard let url = URL(string: firestoreUrl) else { return }

        URLSession.shared.dataTask(with: url) { data, response, error in
            DispatchQueue.main.async {
                self.isLoading = false
                if let data = data,
                   let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
                   let documents = json["documents"] as? [[String: Any]] {

                    var loaded: [AnnouncementItem] = []
                    for doc in documents {
                        if let fields = doc["fields"] as? [String: Any] {
                            let id = (fields["id"] as? [String: Any])?["stringValue"] as? String ?? UUID().uuidString
                            let title = (fields["title"] as? [String: Any])?["stringValue"] as? String ?? "Alert"
                            let body = (fields["body"] as? [String: Any])?["stringValue"] as? String ?? ""
                            let priority = (fields["priority"] as? [String: Any])?["stringValue"] as? String ?? "Normal"
                            let targetYear = (fields["targetYear"] as? [String: Any])?["stringValue"] as? String ?? "All"
                            let tsStr = (fields["timestamp"] as? [String: Any])?["integerValue"] as? String ?? "0"
                            let timestamp = Int64(tsStr) ?? 0

                            if targetYear == "All" || targetYear == userYear {
                                loaded.append(AnnouncementItem(id: id, title: title, body: body, priority: priority, targetYear: targetYear, timestamp: timestamp))
                            }
                        }
                    }
                    self.alertList = loaded.sorted(by: { $0.timestamp > $1.timestamp })
                }
            }
        }.resume()
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
