export type MessageResponse = {
  message: string
}

export type UserSummary = {
  id: string
  email: string
  nickname: string
  active: boolean
}

export type UserProfile = UserSummary & {
  gender: string
  birthYear: number
  height: number
  weight: number
  goal: string
  healthNote: string
}

export type LoginRequest = {
  email: string
  password: string
}

export type SignupRequest = {
  email: string
  password: string
  nickname: string
  gender: string
  birthYear: number | null
  height: number | null
  weight: number | null
  goal: string
  healthNote: string
}

export type AuthResponse = {
  message: string
  user: UserSummary
}

export type UpdateUserProfileRequest = {
  email: string
  nickname: string
  password: string
  gender: string
  birthYear: number | null
  height: number | null
  weight: number | null
  goal: string
  healthNote: string
}

export type UserProfileDashboard = {
  user: UserProfile
  dailyGoal: DailyGoal
  mealCount: number
  followingCount: number
  followerCount: number
  joinedChallengeCount: number
}

export type FoodResponse = {
  code: string
  name: string
  category: string
  grams: number
  energy: number
  carbs: number
  protein: number
  fat: number
}

export type MealNutrition = {
  calories: number
  carbs: number
  protein: number
  fat: number
  carbsPct: number
  proteinPct: number
  fatPct: number
}

export type MealFood = FoodResponse

export type MealFoodSelection = {
  code: string
  grams: number | null
}

export type MealRequest = {
  mealDate: string
  mealType: string
  memo: string
  foods: MealFoodSelection[]
}

export type MealSummary = {
  id: string
  mealDate: string
  mealType: string
  memo: string
  nutrition: MealNutrition
}

export type DailyGoal = {
  calories: number
  carbs: number
  protein: number
  fat: number
}

export type MealAnalysis = {
  nutrition: MealNutrition
  headline: string
  nextAction: string
  grade: string
  score: number
  insights: string[]
}

export type MealDetail = MealSummary & {
  userId: string
  foods: MealFood[]
  analysis: MealAnalysis | null
  dailyGoal: DailyGoal | null
  createdAt: string
  updatedAt: string
}

export type MealSearchParams = {
  startDate?: string
  endDate?: string
  mealType?: string
  sortKey?: string
}

export type ChallengeSummary = {
  id: string
  title: string
  description: string
  targetCount: number
  endDate: string
}

export type CoachSummary = {
  summary: string
  recovery: string
  recentAnalyses: MealAnalysis[]
}

export type HomeDashboard = {
  recentMeals: MealSummary[]
  todaySummary: MealNutrition
  dailyGoal: DailyGoal
  coachAdvice: CoachSummary
  activeChallenges: ChallengeSummary[]
  followingCount: number
  followerCount: number
}

export type CoachWorkoutSession = {
  title: string
  detail: string
  intensity: string
}

export type CoachAnalysisCard = {
  headline: string
  nextAction: string
  grade: string
  score: number
}

export type CoachChallengeCard = {
  id: string
  title: string
  description: string
  progress: number
  targetCount: number
}

export type CoachDashboard = {
  summary: string
  recovery: string
  todaySummary: {
    calories: number
    protein: number
  }
  dailyGoal: {
    calories: number
    protein: number
  }
  todayPct: number
  sessions: CoachWorkoutSession[]
  nextActions: string[]
  recentAnalyses: CoachAnalysisCard[]
  challenges: CoachChallengeCard[]
}

export type CommunityComment = {
  id: string
  postId: string
  userId: string
  authorName: string
  content: string
  createdAt: string
  canEdit: boolean
}

export type CommunityPost = {
  id: string
  userId: string
  authorName: string
  category: string
  categoryLabel: string
  linkedMealId: string | null
  title: string
  content: string
  createdAt: string
  canEdit: boolean
  comments: CommunityComment[]
}

export type CommunityBoard = {
  selectedCategory: string
  meals: MealSummary[]
  posts: CommunityPost[]
}

export type CommunityPostRequest = {
  category: string
  linkedMealId: string
  title: string
  content: string
}

export type CommunityCommentRequest = {
  content: string
}

export type ChallengeParticipant = {
  userId: string
  nickname: string
  progress: number
}

export type ChallengeMembership = {
  id: string
  progress: number
  status: string
}

export type ChallengeItem = {
  id: string
  title: string
  description: string
  category: string
  targetCount: number
  periodLabel: string
  owned: boolean
  membership: ChallengeMembership | null
  statusLabel: string
  participants: ChallengeParticipant[]
}

export type ChallengeBoard = {
  joinedCount: number
  completedCount: number
  createdCount: number
  challengeCount: number
  challenges: ChallengeItem[]
}

export type ChallengeRequest = {
  title: string
  description: string
  category: string
  targetCount: number
  endDate: string
}
